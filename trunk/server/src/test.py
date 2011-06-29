#!/usr/bin/env python
#-*- coding: utf-8 -*-"

'''
Created on 31.01.2011
@author: Eugene Vorobkalo
'''

import proxy_mod.proxy_ctrader as proxy
from pprint import pprint

CLIENT_ID = 508

def test_client():
    data = proxy.GetRefClient(CLIENT_ID)
    for row in data:
        #print row['NAME'], len(row['NAME']) 
        print row


def show_field_info(table_name):
    print '''
== Параметры функции ==
 * *AgentID* - INTEGER Уникальный идентификатор торгового агента.

== Результат выполнения функции ==
 Массив (список) где каждая запись "Словарь" (ключ, значение):
'''    
    
    import sqlite3
    import string
    db = sqlite3.connect(r'd:\DroidPres.sqlite')
    cur = db.cursor()
    cur.execute("PRAGMA table_info(" + table_name + ")")
    data = cur.fetchall()
    cur.close()
    
    max_len1, max_len2 = 0,0
    for row in data:
        if len(row[1]) > max_len1: max_len1 = len(row[1])   
        if len(row[2]) > max_len2: max_len2 = len(row[2])
    print '|| %s || %s || %s ||'  % (string.ljust(u'*Ключ*', max_len1), string.ljust(u'*Тип*', max_len2), u'*Описание*')
    for row in data:
        col1 = row[1] 
        if col1 == '_id': col1 = '`_`id' 
        print '|| %s || %s ||  ||'  % (string.ljust(col1, max_len1), string.ljust(row[2], max_len2))
    

if __name__ == '__main__':
    #test_client()
    show_field_info('document_det')