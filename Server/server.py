#!/usr/bin/env python
# coding: utf-8
import socket
import multiprocessing
from time import localtime
import time
import struct
from multiprocessing import Queue
import json
import random
import pyqrcode 
import sys

BUFSIZ = 24

def get_ip_address():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    return s.getsockname()[0]


def generate_QRCode(ip_address):
    url = pyqrcode.create(ip_address) 
    url.show()


def socket_for_holovitality(port_for_holovitality, q_to_HR, q_to_VR, filepath):
    
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    addr = (local_host, port_for_holovitality)
    sock.bind(addr)
    sock.listen(2)

    print('Wait for HoloVitality, port: %d' % (port_for_holovitality))
    # tcpClientSock, (addr_from_phone, port1) = sock.accept()
    # print(addr_from_phone)
    is_Receiving = True

    count = 0
    cur_time = int(round(time.time() * 1000))
    with open(filepath+'data.json', 'w') as f:
        while is_Receiving:
            try:
                # stream_data = tcpClientSock.recv(BUFSIZ)
                # data = struct.unpack("2d1l", stream_data)
                heart_rate = random.randint(50, 150)
                variance = random.randint(0, 20)
                data = (heart_rate, variance, cur_time)
                cur_time+=1000
                heart_rate, variance, timestamp = data
                timestamp = timestamp // 1000
                # print(heart_rate, variance, timestamp)
                q_to_HR.put(heart_rate)
                q_to_VR.put(variance)
                # write to file
                jsondata = {'timestamp': timestamp, 'heart_rate': heart_rate, 'variance': variance}
                f.write(json.dumps(jsondata)+'\n')

                count += 1
            except Exception as e:
                print(e)
                break
            if data is None or count >= 100:
                break
    f.close()
    # tcpClientSock.close()
    sock.close()

def transport_HR(port_for_holelens, q_from_HR):
    
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    addr = (local_host, port_for_holelens)
    sock.bind(addr)
    sock.listen(2)

    print('Wait for HoloLens connect to HR port: %d' % (port_for_holelens))
    tcpClientSock, (addr_from_hololens, port1) = sock.accept()
    print(addr_from_hololens)
    
    while True:
        while q_from_HR.empty():
            continue
        heart_rate = q_from_HR.get_nowait()
        
        print('Transport HR: ' + str(heart_rate))
        data_bytes = struct.pack("1d", heart_rate)
        tcpClientSock.send(data_bytes)

    sock.close()


def transport_VR(port_for_holelens, q_from_VR):
    
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    addr = (local_host, port_for_holelens)
    sock.bind(addr)
    sock.listen(2)

    print('Wait for HoloLens connect to VR port: %d' % (port_for_holelens))
    tcpClientSock, (addr_from_hololens, port1) = sock.accept()
    print(addr_from_hololens)
    
    while True:
        while q_from_VR.empty():
            continue
        variance = q_from_VR.get_nowait()
        
        print('Transport VR: ' + str(variance))
        data_bytes = struct.pack("1d", variance)
        tcpClientSock.send(data_bytes)

    sock.close()

local_host = get_ip_address()
print('Server IP: ' + local_host)
generate_QRCode(local_host)


HR_queue = Queue()
VR_queue = Queue()
socket_process = multiprocessing.Process(target=socket_for_holovitality, args=(12345, HR_queue, VR_queue, '/Users/jguo/Desktop/'))
socket_process.start()
transport_HR_process = multiprocessing.Process(target=transport_HR, args=(12346, HR_queue))
transport_HR_process.start()
transport_VR_process = multiprocessing.Process(target=transport_VR, args=(12347, VR_queue))
transport_VR_process.start()