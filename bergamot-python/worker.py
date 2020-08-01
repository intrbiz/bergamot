#!/usr/bin/python3

from bergamot.worker import BergamotWorker
from bergamot.worker.engine.dummy import *
from bergamot.config import load_worker_config


worker = BergamotWorker()

worker.register_engine(BergamotDummyWorkerEngine())

worker.configure(load_worker_config())
worker.run()
