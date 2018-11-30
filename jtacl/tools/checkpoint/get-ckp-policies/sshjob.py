"""Define a job running via SSH"""

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

import sys
import time
import jobs
import spur
import shlex

class SshJob(jobs.Job):
    """A job running via SSH"""

    def __init__(self, host, user, key, command, jobstr, start_callback, done_callback):
        """
        Create a new job to run via SSH
        :param host: ssh host
        :param user: ssh user
        :param key: ssh private key
        :param command: command to run via ssh
        :param jobstr: a textual documentation of the job
        :param start_callback: callback to call when the job starts
        :param done_callback: callback to call when the job terminates
        """
        super()
        self._host = host
        self._user = user
        self._key = key
        self._command = command
        self._jobstr = jobstr
        self._start_callback = start_callback
        self._done_callback = done_callback
        self._ssh = None
        self._process = None
        self._result = None

    @property
    def result(self):
        return self._result

    def _newssh(self):
        self._ssh = spur.SshShell(
            hostname=self._host,
            username=self._user,
            private_key_file= self._key
        )

    def run(self):
        self.spawn()
        self.wait_for()
        return self

    def spawn(self):
        self._newssh()
        self.spawn_time = time.time()
        args = list(shlex.shlex(self._command, posix=True, punctuation_chars=True))
        self.start_callback()
        self._process = self._ssh.spawn(args)
        return self

    def wait_for(self):
        self._result = self._process.wait_for_result()
        self.done_callback()
        return self

    def is_running(self):
        if self._process is None:
            return False
        return self._process.is_running()

    def start_callback(self):
        if self._start_callback is not None:
            self._start_callback(self)

    def done_callback(self):
        if self._done_callback is not None:
            self._done_callback(self)

    def timeout_error(self):
        print('', file=sys.stderr, flush=True)
        print('Error job timeout !: ' + self._command, file=sys.stderr, flush=True)

    def __str__(self):
        return "job #" + str(self.jobnumber) + ", " + self._jobstr
