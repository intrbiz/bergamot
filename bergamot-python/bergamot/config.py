
import os

def get_property(namespace, name):
    return os.getenv('BERGAMOT_' + namespace + '_' + name)

def get_required_property(namespace, name, message):
    val = get_property(namespace, name);
    if not val:
        raise Exception(message)
    return val

def load_config(namespace):
    config = {}
    config['url'] = get_required_property(namespace, 'URL', 'The ' + namespace.lower() + ' server URL must be provided')
    config['auth_key'] = get_required_property(namespace, 'KEY', 'The ' + namespace.lower() + ' authentication key must be provided')
    return config

def load_agent_config():
    # { 'agent_id': '2ab19637-19fc-4cae-9935-a85d82c22b96', 'url': 'ws://127.0.0.1:15080/agent', 'auth_key': 'G6FZvsE7ij9KrIHRUh2HcM0TLkdThlFA9ldBI9IP6NjZSPCX1XzjsHr8a3f3iskyndI', 'template_name': None, 'info': None }
    config = load_config('AGENT');
    config['agent_id'] = get_required_property('AGENT', 'ID', 'The agent id must be provided')
    template_name = get_property('AGENT', 'TEMPLATE_NAME')
    if template_name:
        config['template_name'] = template_name
    info = get_property('AGENT', 'HOST_SUMMARY')
    if info:
        config['info'] = info
    return config

def load_worker_config():
    # { 'url': 'ws://127.0.0.1:14080/proxy', 'auth_key': 'G7EiyxnJRkxGpqvZ5cYg5ygz0eKHFdegce8cqEgEdTCRN0nBc_cqkBWKt82N4MuE9H4' }
    config = load_config('WORKER');
    return config
