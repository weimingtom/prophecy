#!C:/python26/python.exe
# -*- coding:utf-8 -*-

# struct是python的二进制数据打包模块
# 用法和格式化字符串类似
import struct
import sys
import socket

# 打包
# 整型
def UshortToBinary(num):
    return struct.pack('H', num) # 低位在前
def UintToBinary(num):
    return struct.pack('!I', num)
def UTF8ToBinary(data):
    return UshortToBinary(len(data)) + data
def PING(msg):
    result = UshortToBinary(2 + len(msg)) + msg
    print(repr(result))
    return result

# 2 + len(msg)

#print("Enter a string:")
#str = sys.stdin.readline().rstrip()
#print repr(UTF8ToBinary(str))

def testpack():
    # 创建套接字
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    except socket.error, e:
        print("Error: Create socket error: %s" % e)
        return 1
    # 连接服务器
    try:
        port = 9999
        host = 'localhost'
        s.connect((host, port))
    except socket.gaierror, e:
        print("Error: Address-related error connecting to server: %s" % e)
        return 1
    except socket.error, e:
        print("Error: Connection error: %s" % e)
        print("\thost: %s" % host)
        print("\tport: %d" % port)
        return 1
    # 发送包
    try:
        s.sendall(PING('nanami, ok!'))
    except socket.error, e:
        print("Error: Error sending data %s" % e)
        return 1
    print('Success.')
    return 0

# 循环
for i in range(2):
    if testpack() != 0:
        break


