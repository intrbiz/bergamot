import java.util.UUID;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;


public class QueueTest
{
    public static void main(String[] args)
    {
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool("amqp://127.0.0.1"));
        //
        WorkerQueue queue = WorkerQueue.open();
        Consumer<ExecuteCheck> consumer = queue.consumeChecks((event) -> {
            System.out.println("Event: " + event);
        }, UUID.randomUUID(), null, "nagios");
    }
}
