#!/usr/bin/env python
# coding: utf-8
import socket
import multiprocessing
from time import localtime
import time
import struct
from multiprocessing import Queue
import json

BUFSIZ = 24


def socket_for_holovitality(port_for_holovitality, q_to_raw_data, filepath):
    """
    :param local_host: ip address for python
    :param port_for_ultrasonic: the port set for receiving ultrasonic stream
    :param q_to_raw_data: the queue for raw data
    :param filepath: file path for data saving
    :return:
    """
    
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    host_name = socket.gethostname() 
    local_host = socket.gethostbyname(host_name) 
    print(local_host)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    addr = (local_host, port_for_holovitality)
    sock.bind(addr)
    sock.listen(2)

    print('Wait for HoloVitality, port: %d' % (port_for_holovitality))
    tcpClientSock, (addr_from_phone, port1) = sock.accept()
    print(addr_from_phone)
    is_Receiving = True
    with open(filepath+'data.json', 'a') as f:
        while is_Receiving:
            try:
                stream_data = tcpClientSock.recv(BUFSIZ)
                data = struct.unpack("2d1l", stream_data)
                heart_rate, variance, timestamp = data
                timestamp = timestamp // 1000
                print(heart_rate, variance, timestamp)
                q_to_raw_data.put(data)
                # write to file
                jsondata = {'timestamp': timestamp, 'heart_rate': heart_rate, 'variance': variance}
                json.dump(jsondata, f)
            except Exception as e:
                print(e)
                tcpClientSock.close()
                break
            if data is None:
                break
    f.close()
    sock.close()

def transport(port_for_holelens, q_from_raw_data):
    """
    :param local_host: ip address for python
    :param port_for_ultrasonic: the port set for receiving ultrasonic stream
    :param q_to_raw_data: the queue for raw data
    :param filepath: file path for data saving
    :return:
    """
    
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    host_name = socket.gethostname() 
    local_host = socket.gethostbyname(host_name) 
    print(local_host)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    addr = (local_host, port_for_holelens)
    sock.bind(addr)
    sock.listen(2)

    print('Wait for HoloLens, port: %d' % (port_for_holelens))
#     tcpClientSock, (addr_from_hololens, port1) = sock.accept()
#     print(addr_from_hololens)
    
    while True:
        while q_from_raw_data.empty():
            continue
        current_data = q_from_raw_data.get_nowait()
        
        heart_rate, variance, timestamp = current_data
        print(current_data)
        data_bytes = struct.pack("2d1l", heart_rate, variance, timestamp)
        
#         tcpClientSock.send(data_bytes)

    sock.close()



queue = Queue()
socket_process = multiprocessing.Process(target=socket_for_holovitality, args=(12345, queue, '~/Desktop/'))
socket_process.start()
transport_process = multiprocessing.Process(target=transport, args=(12346, queue))
transport_process.start()