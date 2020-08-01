import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.io.BergamotAgentTranscoder;
import com.intrbiz.bergamot.model.message.ParameterMO;

public class PythonMessageGenerator
{

    public static void main(String[] args) throws Exception
    {
        List<MessageClass> messages = new LinkedList<MessageClass>();
        for (Class<?> type : BergamotAgentTranscoder.BASE_CLASSES)
        {
            MessageClass py = generateMessageType(type);
            if (py != null)
            {
                messages.add(py);
                System.out.println();
                System.out.println(py.content);
            }
        }
        for (Class<?> type : BergamotAgentTranscoder.CLASSES)
        {
            MessageClass py = generateMessageType(type);
            if (py != null)
            {
                messages.add(py);
                System.out.println();
                System.out.println(py.content);
            }
        }
        // decoder method
        System.out.println("def decode_agent_message(message):");
        System.out.println("    type_name = message.get('type')");
        boolean ne = false;
        for (MessageClass msg : messages)
        {
            System.out.print("    ");
            if (ne) System.out.print("el");
            System.out.println("if type_name == '" + msg.messageType + "':");
            System.out.println("        return " + msg.className + "(message)");
            ne = true;
        }
        System.out.println("    return None\n");
    }
    
    private static MessageClass generateMessageType(Class<?> type) throws Exception
    {
        JsonTypeName typeName = type.getAnnotation(JsonTypeName.class);
        if (typeName == null) return null;
        Map<String, Field> fields = getAllJsonFields(type);
        // generate
        StringBuilder sb = new StringBuilder();
        sb.append("class ").append(type.getSimpleName()).append(":\n");
        sb.append("\n");
        // type name
        sb.append("    TYPE_NAME = '").append(typeName.value()).append("'\n");
        sb.append("\n");
        // init
        sb.append("    def __init__(self, message = None):\n");
        sb.append("        if message:\n");
        sb.append("            self.message = message\n");
        // handling of complex fields
        for (Entry<String, Field> field : fields.entrySet())
        {
            if (isSimpleType(field.getValue().getType()))
            {
                // nothing
            }
            else if (List.class == field.getValue().getType())
            {
                ParameterizedType typeInfo = (ParameterizedType) field.getValue().getGenericType();
                Class<?> elementType = ((Class<?>)typeInfo.getActualTypeArguments()[0]);
                if (! isSimpleType(elementType))
                {
                    sb.append("            l = []\n");
                    sb.append("            for x in self.message.get('").append(field.getKey()).append("', []):\n");
                    sb.append("                l.append(decode_agent_message(x))\n");
                    sb.append("            self.message['").append(field.getKey()).append("'] = l\n");
                }
            }
            else if (Map.class == field.getValue().getType())
            {
                // nothing
            }
            else
            {
                sb.append("            v = self.message.get('").append(field.getKey()).append("')\n");
                sb.append("            if v:\n");
                sb.append("                self.message['").append(field.getKey()).append("'] = decode_agent_message(v)\n");
            }
        }
        sb.append("        else:\n");
        sb.append("            self.message = {}\n");
        sb.append("\n");
        // type_name
        sb.append("    def get_type_name(self):\n");
        sb.append("        return ").append(type.getSimpleName()).append(".TYPE_NAME\n");
        sb.append("\n");
        // to_message
        sb.append("    def to_message(self):\n");
        sb.append("        self.message['type'] = ").append(type.getSimpleName()).append(".TYPE_NAME\n");
        // handling of complex fields
        for (Entry<String, Field> field : fields.entrySet())
        {
            if (isSimpleType(field.getValue().getType()))
            {
                // nothing
            }
            else if (List.class == field.getValue().getType())
            {
                ParameterizedType typeInfo = (ParameterizedType) field.getValue().getGenericType();
                Class<?> elementType = ((Class<?>)typeInfo.getActualTypeArguments()[0]);
                if (! isSimpleType(elementType))
                {
                    sb.append("        l = []\n");
                    sb.append("        for x in self.message.get('").append(field.getKey()).append("', []):\n");
                    sb.append("            l.append(x.to_message())\n");
                    sb.append("        self.message['").append(field.getKey()).append("'] = l\n");
                }
            }
            else if (Map.class == field.getValue().getType())
            {
                // nothing
            }
            else
            {
                sb.append("        v = self.message.get('").append(field.getKey()).append("')\n");
                sb.append("        if v:\n");
                sb.append("            self.message['").append(field.getKey()).append("'] = v.to_message()\n");
            }
        }
        sb.append("        return self.message\n");
        sb.append("\n");
        // fields
        for (Entry<String, Field> field : fields.entrySet())
        {
            if (isSimpleType(field.getValue().getType()))
            {
                sb.append("    # ").append(field.getKey()).append(" ").append(field.getValue().getType().getSimpleName()).append("\n");
                sb.append("\n");
                sb.append("    def ").append(field.getKey().replace('-', '_')).append("(self):\n");
                sb.append("        return self.message.get('").append(field.getKey()).append("')\n");
                sb.append("\n");
                sb.append("    def with_").append(field.getKey().replace('-', '_')).append("(self, val):\n");
                sb.append("        self.message['").append(field.getKey()).append("'] = val\n");
                sb.append("        return self\n");
                sb.append("\n");
            }
            else if (List.class == field.getValue().getType())
            {
                ParameterizedType typeInfo = (ParameterizedType) field.getValue().getGenericType();
                Class<?> elementType = ((Class<?>)typeInfo.getActualTypeArguments()[0]);
                // generic list
                sb.append("    # ").append(field.getKey()).append(" List<").append(elementType.getSimpleName()).append(">\n");
                sb.append("\n");
                sb.append("    def ").append(field.getKey().replace('-', '_')).append("(self):\n");
                sb.append("        return self.message.get('").append(field.getKey()).append("', [])\n");
                sb.append("\n");
                sb.append("    def with_").append(field.getKey().replace('-', '_')).append("(self, value):\n");
                sb.append("        if not self.message.get('").append(field.getKey()).append("'):\n");
                sb.append("            self.message['").append(field.getKey()).append("'] = []\n");
                sb.append("        self.message['").append(field.getKey()).append("'].append(value)\n");
                sb.append("        return self\n");
                sb.append("\n");
                // special handling for ParameterMO
                if (ParameterMO.class == elementType)
                {
                    sb.append("    def ").append(field.getKey().replace('-', '_')).append("_value(self, name):\n");
                    sb.append("        vals = self.message.get('").append(field.getKey()).append("', {})\n");
                    sb.append("        for val in vals:\n");
                    sb.append("            if val.name() == name:\n");
                    sb.append("                return val\n");
                    sb.append("        return None\n");
                    sb.append("\n");
                    sb.append("    def with_").append(field.getKey().replace('-', '_')).append("_value(self, name, value):\n");
                    sb.append("        if not self.message.get('").append(field.getKey()).append("'):\n");
                    sb.append("            self.message['").append(field.getKey()).append("'] = []\n");
                    sb.append("        self.message['").append(field.getKey()).append("'].append(ParameterMO().with_name(name).with_value(value))\n");
                    sb.append("        return self\n");
                    sb.append("\n");
                }
            }
            else if (Map.class == field.getValue().getType())
            {
                sb.append("    # ").append(field.getKey()).append(" Map<").append(">\n");
                sb.append("\n");
                sb.append("    def ").append(field.getKey().replace('-', '_')).append("(self):\n");
                sb.append("        return self.message.get('").append(field.getKey()).append("', {})\n");
                sb.append("\n");
                sb.append("    def ").append(field.getKey().replace('-', '_')).append("_value(self, name):\n");
                sb.append("        return self.message.get('").append(field.getKey()).append("', {}).get(name)\n");
                sb.append("\n");
                sb.append("    def with_").append(field.getKey().replace('-', '_')).append("(self, name, value):\n");
                sb.append("        if not self.message.get('").append(field.getKey()).append("'):\n");
                sb.append("            self.message['").append(field.getKey()).append("'] = {}\n");
                sb.append("        self.message['").append(field.getKey()).append("'][name] = value\n");
                sb.append("        return self\n");
                sb.append("\n");
            }
            else
            {
                sb.append("    # ").append(field.getKey()).append(" ").append(field.getValue().getType().getSimpleName()).append("\n");
                sb.append("\n");
                sb.append("    def ").append(field.getKey().replace('-', '_')).append("(self):\n");
                sb.append("        return self.message.get('").append(field.getKey()).append("')\n");
                sb.append("\n");
                sb.append("    def with_").append(field.getKey().replace('-', '_')).append("(self, val):\n");
                sb.append("        self.message['").append(field.getKey()).append("'] = val\n");
                sb.append("        return self\n");
                sb.append("\n");
            }
        }
        //
        return new MessageClass(typeName.value(), type.getSimpleName(),  sb.toString());
    }
    
    private static boolean isSimpleType(Class<?> type)
    {
        return String.class == type ||
                UUID.class == type ||
                int.class == type || Integer.class == type ||
                long.class == type || Long.class == type ||
                float.class == type || Float.class == type ||
                double.class == type || Double.class == type ||
                boolean.class == type || Boolean.class == type || 
                Date.class == type ||
                Timestamp.class == type;
    }
    
    private static Map<String, Field> getAllJsonFields(Class<?> type) throws Exception
    {
        Map<String, Field> fields = new LinkedHashMap<>();
        getAllJsonFields(type, fields);
        return fields;
    }
    
    private static void getAllJsonFields(Class<?> type, Map<String, Field> fields) throws Exception
    {
        // go up
        if (type.getSuperclass() != null)
            getAllJsonFields(type.getSuperclass(), fields);
        // our fields
        for (Field field : type.getDeclaredFields())
        {
            JsonProperty json = field.getAnnotation(JsonProperty.class);
            if (json != null)
            {
                fields.put(json.value(), field);
            }
        }
    }
    
    private static class MessageClass
    {
        public String messageType;
        
        public String className;
        
        public String content;
        
        public MessageClass(String messageType, String className, String content)
        {
            this.messageType = messageType;
            this.className = className;
            this.content = content;
        }
    }
}
