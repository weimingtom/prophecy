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
    return struct.pack('!H', num)
def UintToBinary(num):
    return struct.pack('!I', num)
def UTF8ToBinary(data):
    return UshortToBinary(len(data)) + data
def NMSG_LOGIN(username, password):
    result = 'CT' + UintToBinary(2 + 2 + len(username) + 2 + len(password)) + \
        '\x05\x01' + \
        UTF8ToBinary(username) + UTF8ToBinary(password)
    print(repr(result))
    return result
def NMSG_MOVE(username, x, y):
    result = 'CT' + UintToBinary(2 + 2 + len(username) + 4 + 4) + \
        '\x05\x03' + \
        UTF8ToBinary(username) + UintToBinary(x) + UintToBinary(y)
    print(repr(result))
    return result

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
        port = 8899
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
        s.sendall(NMSG_LOGIN('nanami', '12345'))
        s.sendall(NMSG_MOVE('nanami', 1, 3))
    except socket.error, e:
        print("Error: Error sending data %s" % e)
        return 1
    print('Success.')
    return 0

# 循环
for i in range(0, 100):
    if testpack() != 0:
        break


