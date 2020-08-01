
from datetime import datetime
import traceback

def log(level, source, message, error=None):
    if error:
        print("%s %s %s: %s - %s" % (level, datetime.now(), source, message, error))
        print(traceback.format_exc())
    else:
        print("%s %s %s: %s" % (level, datetime.now(), source, message))
