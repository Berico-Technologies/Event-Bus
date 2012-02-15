package pegasus.eventbus.topology.admin;

public class RabbitAdmin {

    /**
     * Usage: rabbitmqadmin.txt [options] cmd
     * where cmd is one of:
     *   list connections [<column>...]
     *   list channels [<column>...]  list exchanges [<column>...]
     *   list queues [<column>...]  list bindings [<column>...]
     *   list users [<column>...]  list vhosts [<column>...]
     *   list permissions [<column>...]  list nodes [<column>...]
     *   show overview [<column>...]
     *   declare queue name=... [auto_delete=... durable=...]  declare vhost name=...
     *   declare user name=... password=... tags=...  declare permission vhost=... user=... configure=... write=... read=...
     *   declare binding source=... destination_type=... destination=... routing_key=...  declare exchange name=... type=... [auto_delete=... internal=... durable=...]
     *   delete queue name=...
     *   delete vhost name=...  delete user name=...
     *   delete permission vhost=... user=...  delete binding source=... destination_type=... destination=... properties_key=...
     *   delete exchange name=...
     *   close connection name=...
     *   purge queue name=...
     *   publish routing_key=... [payload=... payload_encoding=... exchange=...]  get queue=... [payload_file=... count=... requeue=...]
     *   export <file>
     *   import <file>
     *   * If payload is not specified on publish, standard input is used
     *   * If payload_file is not specified on get, the payload will be shown on    standard output along with the message metadata
     *   * If payload_file is specified on get, count must not be set
     * Options:
     *   -h, --help            show this help message and exit  -H HOST, --host=HOST  connect to host HOST [default: localhost]
     *   -P PORT, --port=PORT  connect to port PORT [default: 55672]  -V VHOST, --vhost=VHOST
     *                         connect to vhost VHOST [default: all vhosts for list,                        '/' for declare]
     *   -u USERNAME, --username=USERNAME                        connect using username USERNAME [default: guest]
     *   -p PASSWORD, --password=PASSWORD                        connect using password PASSWORD [default: guest]
     *   -q, --quiet           suppress status messages  -s, --ssl             connect with ssl
     *   --ssl-key-file=SSL_KEY_FILE                        PEM format key file for SSL [default: none]
     *   --ssl-cert-file=SSL_CERT_FILE                        PEM format certificate file for SSL [default: none]
     *   -f FORMAT, --format=FORMAT                        format for listing commands - one of [long, raw_json,
     *                         tsv, kvp, bash, pretty_json, table]  [default: table]  -d DEPTH, --depth=DEPTH
     *                         maximum depth to recurse for listing tables [default:                        1]
     *   --bash-completion     Print bash completion script
     */
    
//    private static final String PYTHON_EXECUTABLE = "ipy64.exe";
//    private static final String RABBITMQADMIN_SCRIPT = "rabbitmqadmin";
//    private static final String HOST = "localhost";
//    private static final String PORT = "55672";
//    private static final String USERNAME = "guest";
//    private static final String PASSWORD = "guest";
//    private static final String VHOST = "/";
//
//    private String pythonExecutable;
//    private String rabbitMqAdminScript;
//    private String host;
//    private String port;
//    private String username;
//    private String password;
//    
//    public String getPythonExecutable() {
//        return pythonExecutable;
//    }
//
//    public void setPythonExecutable(String pythonExecutable) {
//        this.pythonExecutable = pythonExecutable;
//    }
//
//    public String getRabbitMqAdminScript() {
//        return rabbitMqAdminScript;
//    }
//
//    public void setRabbitMqAdminScript(String rabbitMqAdminScript) {
//        this.rabbitMqAdminScript = rabbitMqAdminScript;
//    }
//
//    public String getHost() {
//        return host == null ? HOST : host;
//    }
//
//    public void setHost(String host) {
//        this.host = host;
//    }
//
//    public String getPort() {
//        return port == null ? PORT : port;
//    }
//
//    public void setPort(String port) {
//        this.port = port;
//    }
//
//    public String getUsername() {
//        return username == null ? USERNAME : username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getPassword() {
//        return password == null ? PASSWORD : password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public ListTable getConnections() {
//        return null;
//    }
//    
//    public ListTable getChannels() {
//        return null;
//    }
//    
//    public ListTable getExchanges(String vhost) {
//        // rabbitmqadmin --host host --port port --vhost vhost list exchanges
//        return null;
//    }
//    
//    public ListTable getQueues() {
//        // rabbitmqadmin -
//        return null;
//    }
//    
//    public ListTable getBindings() {
//        return null;
//    }
//    
//    public ListTable getUsers() {
//        return null;
//    }
//    
//    public ListTable getVhosts() {
//        return null;
//    }
//    
//    public ListTable getPermissions() {
//        return null;
//    }
    
}
