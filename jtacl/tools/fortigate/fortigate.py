#!/usr/bin/env python3

"""
This script retrieves the objects and access rules from a Fortinet Fortigate firewall and output them
in JSON.

The script outputs the result in a single JSON, that will permit lsfw to handle Fortigate equipment

It uses the module pyfortiapi (with a small change) from James Simpson, copyright 2017 license MIT
https://pypi.org/project/PyFortiAPI/

prequisite:
 - python 3
 - module python request
   
configuration:
  The configuration uses a JSON file with the following informations :

{
        "host":"hostname of the fortigate",
        "port":"tcp port to use",
        "user":"user account (a read only account is recommended)",
        "password":"user password",
        "mgmt_password":"managment server user password",
        "vdom":"vdom to use"
}
"""

'''
Copyright (c) 2020  Universite de Rennes 1

Redistribution and use in source and binary forms, with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or other materials
provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
'''

import getopt
import json
import pyfortiapi
import sys

# tool's config
cfg = {}
fg = {}

def writeErr(err):
    print(err, file=sys.stderr, flush=True)

def testresult(result):
    if type(result) == int:
        writeErr("API request failed: " + str(result))
        sys.exit(1)
    return True

def usage():
    writeErr('Usage:')
    writeErr("   -c <config file> configuration for the tools")

def getopts():

    global cfg

    try:
        opts, args = getopt.getopt(sys.argv[1:], "c:", [])
    except getopt.GetoptError as err:
        writeErr(err)
        sys.exit(2)

    config_file = None

    for o, a in opts:
        if (o == '-c'):
            config_file = a

    if config_file is None:
        writeErr("-c <config file> is mandatory")
        usage()
        sys.exit(2)
    jconfig = open(config_file).read()
    cfg = json.loads(jconfig)

    for param in [
        "host",
        "port",
        "user",
        "password",
        "vdom",
    ]:
        if param not in cfg:
            writeErr("missing configuration parameter: " + param + " in file: " + config_file)
            sys.exit(2)

def main():

    getopts()

    device = pyfortiapi.FortiGate(ipaddr=cfg['host'], username=cfg['user'], password=cfg['password'], port=cfg['port'], vdom=cfg['vdom'])
    addresses = device.get_firewall_address()
    if testresult(addresses):
        fg['addresses'] = addresses

    addr_groups = device.get_address_group()
    if testresult(addr_groups):
        fg['addr_groups'] = addr_groups

    services = device.get_firewall_service()
    if testresult(services):
        fg['services'] = services

    serv_groups = device.get_service_group()
    if testresult(serv_groups):
        fg['serv_groups'] = serv_groups

    rules = device.get_firewall_policy()
    if testresult(rules):
        fg['rules'] = rules

    #internet_services = device.get_internet_services()
    #if testresult(internet_services):
    #    fg['internet_services'] = internet_services

    jenc = json.JSONEncoder(indent = 2)
    js = jenc.encode(fg)
    print(js)

if __name__ == "__main__":
    main()
