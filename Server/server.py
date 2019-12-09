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
import numpy as np

BUFSIZ = 24
MEDICAL_BUFSIZ = 300

def get_ip_address():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    return s.getsockname()[0]


def generate_QRCode(ip_address):
    url = pyqrcode.create(ip_address) 
    url.show()


def get_heartbeat_from_client(port_for_holovitality, q_to_HR, q_to_VR, filepath):
    
    
    # while True:
    #     timestamp = round(time.time()*1000)
    #     heart_rate = random.randint(60, 150)
    #     variance = random.randint(0, 10)
    #     q_to_HR.put((heart_rate, timestamp))
    #     q_to_VR.put((variance, timestamp))

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    addr = (local_host, port_for_holovitality)
    sock.bind(addr)
    sock.listen(2)

    print('Wait for HoloVitality client to connect to heartbeat, port: %d' % (port_for_holovitality))


    tcpClientSock, (addr_from_phone, port1) = sock.accept()
    print("Connected to HR: " + addr_from_phone)
    is_Receiving = True

    with open(filepath+'data_'+str(port_for_holovitality)+'.json', 'a') as f:
        while is_Receiving:
            try:
                stream_data = tcpClientSock.recv(BUFSIZ)
                data = struct.unpack("2d1l", stream_data)
                heart_rate, variance, timestamp = data
                timestamp = timestamp // 1000
                print('HR: '+ str(port_for_holovitality))
                q_to_HR.put((heart_rate, timestamp))
                q_to_VR.put((variance, timestamp))
                # write to file
                jsondata = {'timestamp': timestamp, 'heart_rate': heart_rate, 'variance': variance}
                f.write(json.dumps(jsondata)+'\n')

            except Exception as e:
                print(e)
                break
            if data is None:
                break
    f.close()
    tcpClientSock.close()
    sock.close()

def get_medical_from_client(port_for_holovitality, q_to_medical):
    
    
    # while True:
    #     name = "Jason" + " " * 45
    #     allergy = "Dogs" + " " * 46
    #     age = "24" + " " * 48
    #     q_to_medical.put((name.encode()+allergy.encode()+age.encode()))
    #     print(name.encode()+allergy.encode()+age.encode())

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    addr = (local_host, port_for_holovitality)
    sock.bind(addr)
    sock.listen(2)

    print('Wait for HoloVitality client to connect to medical, port: %d' % (port_for_holovitality))
    tcpClientSock, (addr_from_phone, port1) = sock.accept()
    print("Connected to medical: " + addr_from_phone)

    try:
        stream_data = tcpClientSock.recv(MEDICAL_BUFSIZ)
        print(stream_data)
        q_to_medical.put(stream_data)

    except Exception as e:
        print(e)

    tcpClientSock.close()
    sock.close()

def transport_HR(port_for_holelens, q_from_HR):
    while True:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        addr = (local_host, port_for_holelens)
        sock.bind(addr)
        sock.listen(2)

        print('Wait for HoloLens connect to HR port: %d' % (port_for_holelens))
        tcpClientSock, (addr_from_hololens, port1) = sock.accept()
        print("HoloLens HR port: ", port_for_holelens)
        
        while True:
            while q_from_HR.empty():
                continue
            heart_rate, timestamp = q_from_HR.get_nowait()
            if timestamp  < int(round(time.time())) - 1:
                continue
            print('HR: (' + str(port_for_holelens) + ')' + str(heart_rate))
            # print(timestamp, int(round(time.time())))
            data_bytes = struct.pack("1d", heart_rate)
            try:
                tcpClientSock.send(data_bytes)
            except socket.error:
                break

        sock.close()


def transport_VR(port_for_holelens, q_from_VR):
    while True:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        addr = (local_host, port_for_holelens)
        sock.bind(addr)
        sock.listen(2)

        print('Wait for HoloLens connect to VR port: %d' % (port_for_holelens))
        tcpClientSock, (addr_from_hololens, port1) = sock.accept()
        print("HoloLens VR port: ", port_for_holelens)
        
        while True:
            while q_from_VR.empty():
                continue
            variance, timestamp = q_from_VR.get_nowait()
            if timestamp  < int(round(time.time())) - 1:
                continue
            print('VR: (' + str(port_for_holelens) + ')' + str(variance))
            data_bytes = struct.pack("1d", variance)
            try:
                tcpClientSock.send(data_bytes)
            except socket.error:
                break

        sock.close()


def transport_medical(port_for_holelens, q_from_Medical):
    while True:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        addr = (local_host, port_for_holelens)
        sock.bind(addr)
        sock.listen(2)

        print('Wait for HoloLens connect to Medical port: %d' % (port_for_holelens))
        tcpClientSock, (addr_from_hololens, port1) = sock.accept()
        print("HoloLens MR port: ", port_for_holelens)
        
            
        while q_from_Medical.empty():
            continue
        data_bytes = q_from_Medical.get_nowait()
        while True:
            try:
                tcpClientSock.send(data_bytes)
            except Exception:
                break

        sock.close()


def transport_analysis(port_for_holelens):

    
    
    while True:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        addr = (local_host, port_for_holelens)
        sock.bind(addr)
        sock.listen(2)

        print('Wait for HoloLens connect to Analysis port: %d' % (port_for_holelens))
        tcpClientSock, (addr_from_hololens, port1) = sock.accept()
        print("HoloLens Analysis port: ", port_for_holelens)

        heart_rate_transfer = []
        for i in range(num):
            with open('/Users/jguo/Desktop/data_'+str(port[i]+1)+'.json') as json_file:
                heart_rate_array = []
                for line in json_file:
                    data = json.loads(line)
                    heart_rate = data['heart_rate']
                    if np.isnan(heart_rate):
                        heart_rate = 0
                    heart_rate_array.append(heart_rate)
            heart_rate_array = heart_rate_array[-10:]
            heart_rate_transfer = np.append(heart_rate_transfer,heart_rate_array)


        data_bytes = b''
        for i in range(20):
            print(heart_rate_transfer[i])
            data_bytes += struct.pack("1d", heart_rate_transfer[i])
        
        tcpClientSock.send(data_bytes)
        break


local_host = get_ip_address()
print('Server IP: ' + local_host)
# generate_QRCode(local_host)

num = 2
HR_queues = []
VR_queues = []
medical_queues = []
heartbeat_processes = []
medical_processes = []
transport_HR_processes = []
transport_VR_processes = []
transport_medical_processes = []

port = [12345,12355]

for i in range(num):
    HR_queue = Queue()
    VR_queue = Queue()
    medical_queue = Queue()
    HR_queues.append(HR_queue)
    VR_queues.append(VR_queue)
    medical_queues.append(medical_queue)

    medical_process = multiprocessing.Process(target=get_medical_from_client, args=(port[i], medical_queue))
    medical_processes.append(medical_process)
    medical_processes[i].start()
    
    heartbeat_process = multiprocessing.Process(target=get_heartbeat_from_client, args=(port[i]+1, HR_queues[i], VR_queues[i], '/Users/jguo/Desktop/'))
    heartbeat_processes.append(heartbeat_process)
    heartbeat_processes[i].start()

    transport_HR_process = multiprocessing.Process(target=transport_HR, args=(port[i]+2, HR_queues[i]))
    transport_HR_processes.append(transport_HR_process)
    transport_HR_processes[i].start()

    transport_VR_process = multiprocessing.Process(target=transport_VR, args=(port[i]+3, VR_queues[i]))
    transport_VR_processes.append(transport_VR_process)
    transport_VR_processes[i].start()

    transport_medical_process = multiprocessing.Process(target=transport_medical, args=(port[i]+4, medical_queues[i]))
    transport_medical_processes.append(transport_medical_process)
    transport_medical_processes[i].start()

transport_analysis_process = multiprocessing.Process(target=transport_analysis, args=([port[0]+5]))
transport_analysis_process.start()


    
