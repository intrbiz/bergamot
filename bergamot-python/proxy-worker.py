#!/usr/bin/python3

from bergamot.worker import BergamotWorker
from bergamot.worker_dummy import BergamotDummyWorkerEngine


worker = BergamotWorker()
worker.register_engine(BergamotDummyWorkerEngine())
worker.configure()
worker.run()



