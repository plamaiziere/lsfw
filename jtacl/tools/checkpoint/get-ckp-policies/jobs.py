"""A quick & dirty jobs scheduler"""

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

import time

class JobTimeOutError(Exception):
    """Error when a job exceeds time execution"""
    pass

class Job(object):
    """
        Abstract class for job.
    """

    def __init__(self):
        self.jobnumber = 0

    def is_running(self):
        """
        return True if this job is running, false otherwise
        :rtype: bool
        """
        raise NotImplementedError()

    def run(self):
        """
        run this job immediatly and wait for completion
        :return: self
        """
        raise NotImplementedError()

    def spawn(self):
        """
        run this job immediatly but does not wait for completion
        :return: self
        """
        raise NotImplementedError()

    def wait_for(self):
        """
        wait for job completion
        :return: self
        """
        raise NotImplementedError()

    def done_callback(self):
        """
        callback when this job is ended (just after)
        Nb: this is the job responsibility to run this method
        :return:
        """
        raise NotImplementedError()

    def start_callback(self):
        """
        callback when this job is started (just before)
        Nb: this is the job responsibility to run this method
        :return:
        """
        raise NotImplementedError()


class Jobs(object):
    """Quick & dirty jobs scheduler"""
    def __init__(self):
        self._waiting = []
        self._running = []
        self._done = []
        self._jobnumber = 0

    @property
    def waiting(self):
        """jobs waiting to run"""
        return self._waiting

    @property
    def running(self):
        """jobs running"""
        return self._running

    @property
    def done(self):
        """jobs terminated"""
        return self._done

    def queuejob(self, job):
        """
        Queue a job
        :param job: Job to queue
        """
        job.jobnumber = self._jobnumber
        self._jobnumber += 1
        self._waiting.append(job)

    def queuejob_and_run(self, job):
        """
        run a job immediatly and wait for completion
        :param job: job to run
        """
        job.jobnumber = self._jobnumber
        self._jobnumber += 1
        job.run()
        self._done.append(job)

    def run(self, maxjob, timeout):
        """
        run the jobs queued in //
        :param maxjob: max jobs to run simultaneously
        :param timeout: time out in seconds for job completion
        :raise: JobTimeOutError if the execution time of a job exceed `timeout`
        """
        while (len(self._waiting) != 0) or (len(self._running) != 0):
            while (len(self._waiting) != 0) and (len(self._running) < maxjob):
                job = self._waiting[0]
                self._running.append(job)
                self._waiting.remove(job)
                job.spawn()

            done = []
            for job in self._running:
                if not job.is_running():
                    job.wait_for()
                    done.append(job)
                    self._done.append(job)
                else:
                    now = time.time()
                    if now - job.spawn_time > timeout:
                        raise JobTimeOutError

            for job in done:
                self._running.remove(job)

            time.sleep(0.1)

        self._done = sorted(self._done, key=lambda job: job.jobnumber)

