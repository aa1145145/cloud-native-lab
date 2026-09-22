package cn.edu.xmu.cloudnativelab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class CounterController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/")
    public String hello() {
        // Redis 计数器加 1
        Long count = redisTemplate.opsForValue().increment("visitor_count");

        // 获取当前容器/主机的 IP 和主机名 (用于 K8s 验证负载均衡)
        String hostName = "Unknown";
        String hostAddress = "Unknown";
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            hostName = inetAddress.getHostName();
            hostAddress = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // 返回结果页面
        return String.format(
                "<h1>云原生综合实验 - 访问计数器</h1>" +
                        "<p><b>总访问次数:</b> %d</p>" +
                        "<p><b>当前处理请求的节点 (Pod Name):</b> %s</p>" +
                        "<p><b>节点 IP:</b> %s</p>",
                count, hostName, hostAddress
        );
    }
}