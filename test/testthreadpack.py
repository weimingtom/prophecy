#!C:/python26/python.exe
# -*- coding:utf-8 -*-

# struct是python的二进制数据打包模块
# 用法和格式化字符串类似
import struct
import sys
import socket
from threading import Thread
import logging

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

def thread_main(k):
    # 创建套接字
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    except socket.error, e:
        logging.error("Error: Create socket error: %s" % e)
        return 1
    # 连接服务器
    try:
        port = 8899
        host = 'localhost'
        s.connect((host, port))
    except socket.gaierror, e:
        logging.error("Error: Address-related error connecting to server: %s" % e)
        return 1
    except socket.error, e:
        logging.error("Error: Connection error: %s\thost: %s\tport: %d" % (e, host, port))
        return 1
    # 发送包
    try:
        s.sendall(NMSG_LOGIN('nanami', '12345'))
        s.sendall(NMSG_MOVE('nanami', k, 3))
        s.sendall(NMSG_LOGIN('nanami', '12345'))
    except socket.error, e:
        logging.error("Error: Error sending data %s" % e)
        return 1
    logging.info('Success.')
    return 0

# see
# http://www.pythonclub.org/python-basic/threading
def main(num):
    #线程池
    threads = []
    for x in xrange(0, num):
        threads.append(Thread(target=thread_main, args=(x,)))
    for t in threads:
        t.start()
    #等待线程退出
    for t in threads:
        t.join()

if __name__ == '__main__':
    logging.basicConfig(args=(sys.stdout,),level=logging.DEBUG,)
    logging.info('Start multithread clients...')
    main(1000)


