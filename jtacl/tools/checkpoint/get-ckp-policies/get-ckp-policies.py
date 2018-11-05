#!/usr/bin/env python3

"""
This script retrieves the objects and access rules from a CheckPoint security managment server and output them
in JSON.

(Since Gaia R80, the tools cpdb2web is no longer available).

The script connects to the managment server via ssh and run the Checkpoint 'mgmt_cli' tool to export the objects
in JSON. As this is quite slow, we use several jobs in //.

The script outputs the result in a single JSON, that will permit lsfw to handle Checkpoint >= R80.

XXXX: WORK IN PROGRESS!

API mgmt_cli / documentation:
https://sc1.checkpoint.com/documents/latest/APIs/index.html#mgmt_cli~v1.3%20

prequisite:
 - python 3.6 and module spur (ssh)
 - ssh access to the managment server via ssh keys.

configuration:
  The configuration uses a JSON file with the following informations :

{
        "ssh_host":"hostname of the managment server",
        "ssh_key":"path to the ssh private key",
        "ssh_user":"ssh user",
        "mgmt_user":"managment server user name (a read only account is recommended)",
        "mgmt_password":"managment server user password",
        "max_job":number of jobs to run
        "job_timeout":time out in second for a job to complete
}

bugs:
  The tool 'mgmt_cli' fails to connect to the managment server when the managment server is installing a policy
  on gateway.

"""

'''
Copyright (c) 2018  Universite de Rennes 1

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

import sshjob
import jobs
import json
import sys
import getopt

# tool's config
cfg = {}

def writeErr(err):
    print(err, file = sys.stderr, flush = True)

class MgmtCliJob(sshjob.SshJob):

    def __init__(self, host, user, key, command, jobstr, start_callback, done_callback, sshjobs, ckp_object, iteration, iteration_count, nbobjects, outputfile, cmd_options=''):
        super().__init__(
            host = host,
            user = user,
            key = key,
            command = command,
            jobstr = jobstr,
            start_callback = start_callback,
            done_callback = done_callback
        )
        self._sshjobs = sshjobs
        self._ckp_object = ckp_object
        self._iteration = iteration
        self._iteration_count = iteration_count
        self._nbobjects = nbobjects
        self._outputfile = outputfile
        self._cmd_options = cmd_options


    @property
    def sshjobs(self):
        return self._sshjobs

    @property
    def ckp_object(self):
        return self._ckp_object

    @property
    def nbobjects(self):
        return self._nbobjects

    @property
    def iteration(self):
        return self._iteration

    @property
    def iteration_count(self):
        return self._iteration_count

    @property
    def outputfile(self):
        return self._outputfile

    @property
    def cmd_options (self):
        return self._cmd_options

def find_uid(list, uid):
    for o in list:
        if o['uid'] == uid:
            return o
    return None

class CkpConfig(object):

    def __init__(self):
        self.objects_dict = []
        self.access_layers = []

    def add_object_dict(self, ckp_object):
        if find_uid(self.objects_dict, ckp_object['uid']) is None:
            self.objects_dict.append(ckp_object)

    def add_rulebase(self, rulebase):
        l = find_uid(self.access_layers, rulebase['uid'])
        # merge same rules and dict
        rules = l['rulebase']
        rules.extend(rulebase['rulebase'])
        # append objects in the rulebase dictionary to global dict
        for o in rulebase['objects-dictionary']:
            self.add_object_dict(o)

    def add_access_layer(self, layer):
        if find_uid(self.access_layers, layer['uid']) is None:
            layer['rulebase'] = []
            self.access_layers.append(layer)


def mgmt_cli():
    return "mgmt_cli " + "-u " + cfg['mgmt_user'] + " -p " + cfg['mgmt_password']

def new_mgmt_job(command, jobstr, sshjobs, ckp_object, iteration, iteration_count, nbobjects, outputfile, cmd_options=''):
    job = MgmtCliJob(
            host = cfg['ssh_host'],
            user = cfg['ssh_user'],
            key = cfg['ssh_key'],
            command = command,
            jobstr = jobstr,
            sshjobs = sshjobs,
            ckp_object = ckp_object,
            iteration = iteration,
            iteration_count = iteration_count,
            nbobjects = nbobjects,
            outputfile = outputfile,
            cmd_options = cmd_options,
            start_callback = job_start_callback,
            done_callback = job_done_callback
    )
    return job

def ckp_command(ckp_object, cmd_options, limit, offset):
    return "show " + str(ckp_object) + " " + cmd_options + " limit " + str(limit) + " offset " + str(offset) + " details-level full --format json"

# callback called when a job is started
def job_start_callback(job):
    print('+', file = sys.stderr, end = '', flush = True)

# callback called when a job is ended
def job_done_callback(job):

    print('-', file = sys.stderr, end = '', flush = True)
    iteration = job.iteration
    nbobjects = job.nbobjects

    if cfg['output_dir'] is not None:
        filename = cfg['output_dir'] + '/' + job.outputfile + '_' + str(job.iteration) + '.json'
        f = open(filename, 'w')
        js = json.loads(job.result.output)
        jenc = json.JSONEncoder(indent = 2)
        jss = jenc.encode(js)
        f.write(jss)
        f.close()

    # the first iteration is used to know the number of objects to retrieve
    if iteration == 0:
        js = json.loads(job.result.output)
        total = js['total']
        count = round((total / nbobjects) + 0.5)
        iteration += 1
        # queue jobs as needed to retrieve all the objects
        while iteration < count:
            offset = nbobjects * iteration
            cmd = ckp_command(
                ckp_object = job.ckp_object,
                cmd_options = job.cmd_options,
                limit = job.nbobjects,
                offset = offset
            )
            njob = new_mgmt_job(
                command = mgmt_cli() + " " + cmd,
                jobstr = cmd,
                sshjobs = job.sshjobs,
                ckp_object = job.ckp_object,
                iteration = iteration,
                iteration_count= count,
                nbobjects = job.nbobjects,
                outputfile= job.outputfile,
                cmd_options = job.cmd_options
            )
            iteration += 1
            job.sshjobs.queuejob(njob)


def queue_firstcmd(sshjobs, nbobjects, ckp_object, outputfile, cmd_options=''):

    cmd = ckp_command(
        ckp_object=ckp_object,
        cmd_options=cmd_options,
        limit=nbobjects,
        offset=0
    )

    job = new_mgmt_job(
        command=mgmt_cli() + " " + cmd,
        jobstr=cmd,
        sshjobs=sshjobs,
        ckp_object=ckp_object,
        iteration=0,
        iteration_count=1,
        nbobjects=nbobjects,
        outputfile = outputfile,
        cmd_options=cmd_options
    )

    sshjobs.queuejob(job)

# retreive the results from jobs
def get_jobs_result(sshjobs, ckpconfig):

    for job in sshjobs.done:
        js = json.loads(job.result.output)

        if job.ckp_object == 'access-rulebase':
            ckpconfig.add_rulebase(js)
        elif job.ckp_object == 'access-layers':
            for layer in js['access-layers']:
                ckpconfig.add_access_layer(layer)
        else:
            for o in js['objects']:
                ckpconfig.add_object_dict(o)

def usage():
    writeErr('Usage:')
    writeErr("   -c <config file> configuration for the tools")
    writeErr("   -o <output directory> directory to store output json files from mgmt_cli command (optionnal)")

def getopts():

    global cfg

    try:
        opts, args = getopt.getopt(sys.argv[1:], "c:o:", [])
    except getopt.GetoptError as err:
        writeErr(err)
        sys.exit(2)

    config_file = None
    output_dir = None
    for o, a in opts:
        if (o == '-c'):
            config_file = a

        if (o == '-o'):
            output_dir = a

    if config_file is None:
        writeErr("-c <config file> is mandatory")
        usage()
        sys.exit(2)
    jconfig = open(config_file).read()
    cfg = json.loads(jconfig)

    for param in ["ssh_host", "ssh_user", "ssh_key", "mgmt_user", "mgmt_password", "max_job", "job_timeout"]:
        if param not in cfg:
            writeErr("missing configuration parameter: ", param, " in file: ", config_file)
            sys.exit(2)

    cfg['output_dir'] = output_dir

def main():

    getopts()

    # the resulting Checkpoint config
    ckpconfig = CkpConfig()

    # rules layers are needed to retrieve access rules set
    sshjobs = jobs.Jobs()
    queue_firstcmd(
        sshjobs=sshjobs,
        nbobjects=cfg['access_layers_nbobjects'],
        ckp_object="access-layers",
        outputfile= 'access-layers'
    )
    sshjobs.run(cfg['max_job'], cfg['job_timeout'])

    # parse layers when jobs are done
    get_jobs_result(sshjobs = sshjobs, ckpconfig = ckpconfig)

    # basic jobs scheduler for ssh command
    sshjobs = jobs.Jobs()

    # access rules per layer
    for layer in ckpconfig.access_layers:
        queue_firstcmd(
            sshjobs=sshjobs,
            nbobjects=cfg["access_rulebase_nbobjects"],
            ckp_object='access-rulebase',
            outputfile = 'access-rulebase_' + layer['uid'],
            cmd_options='uid ' + layer['uid'] + ' ' + 'use-object-dictionary true'
        )

    # hosts
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['hosts_nbobjects'], ckp_object="hosts", outputfile = 'hosts')

    # groups
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['groups_nbobjects'], ckp_object="groups", outputfile = 'groups')

    # groups with exclusion
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['groups_nbobjects'], ckp_object="groups-with-exclusion", outputfile = 'groups-with-exclusion')

    # networks
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['networks_nbobjects'], ckp_object="networks", outputfile= 'networks')

    # address ranges
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['address_ranges_nbobjects'], ckp_object="address-ranges", outputfile = 'address-ranges')

    # multicast address ranges
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['multicast_address_ranges_nbobjects'], ckp_object="multicast-address-ranges", outputfile= 'multicast-address-ranges')

     # gateways
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['simple_gateways_nbobjects'], ckp_object="simple-gateways", outputfile='simple-gateways')

    # services
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['services_nbobjects'], ckp_object="services-tcp", outputfile='services-tcp')
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['services_nbobjects'], ckp_object="services-udp", outputfile='services-udp')
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['services_nbobjects'], ckp_object="services-icmp", outputfile='services-icmp')
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['services_nbobjects'], ckp_object="services-icmp6", outputfile='services-icmp6')
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['services_nbobjects'], ckp_object="services-other", outputfile='services-other')
    queue_firstcmd(sshjobs=sshjobs, nbobjects=cfg['services_nbobjects'], ckp_object="service-groups", outputfile='service-groups')

    # run jobs in //
    sshjobs.run(cfg['max_job'], cfg['job_timeout'])
    print('', file=sys.stderr, flush=True)

    get_jobs_result(sshjobs = sshjobs, ckpconfig = ckpconfig)

    # export to json
    ckp = {}
    ckp['objects-dictionary'] = ckpconfig.objects_dict
    ckp['layers'] = ckpconfig.access_layers
    jenc = json.JSONEncoder(indent = 2)
    js = jenc.encode(ckp)
    print(js)

if __name__ == "__main__":
    main()