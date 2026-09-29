package cn.edu.xmu.cloudnativelab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CounterController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    // 接口 1：核心业务（带 UI 渲染，直观查看负载均衡）
    @GetMapping("/")
    public String hello() {
        Long count = redisTemplate.opsForValue().increment("visitor_count");
        String hostName = getHostName();
        String hostAddress = getHostAddress();

        return String.format(
                "<div style='font-family: Arial; text-align: center; margin-top: 50px;'>" +
                        "<h1> 微服务节点</h1>" +
                        "<h2 style='color: #4CAF50;'>总访问次数: %d</h2>" +
                        "<hr style='width: 50%%;'>" +
                        "<p><b>当前处理请求的 Pod (主机名):</b> %s</p>" +
                        "<p><b>Pod IP 地址:</b> %s</p>" +
                        "<p><b>连接的 Redis 地址:</b> %s</p>" +
                        "</div>",
                count, hostName, hostAddress, redisHost
        );
    }

    // 接口 2：JSON 格式的信息接口 (标准的 RESTful API)
    @GetMapping("/api/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("app_name", "cloud-native-lab");
        info.put("pod_name", getHostName());
        info.put("pod_ip", getHostAddress());
        info.put("redis_host", redisHost);
        info.put("timestamp", System.currentTimeMillis());
        return info;
    }

    // 接口 3：健康检查接口
    @GetMapping("/api/health")
    public Map<String, String> healthCheck() {
        Map<String, String> status = new HashMap<>();
        try {
            // 简单测试 Redis 连通性
            redisTemplate.getConnectionFactory().getConnection().ping();
            status.put("status", "UP");
            status.put("redis", "CONNECTED");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("redis", "DISCONNECTED");
        }
        return status;
    }

    // 辅助方法：获取主机名
    private String getHostName() {
        try { return InetAddress.getLocalHost().getHostName(); }
        catch (UnknownHostException e) { return "Unknown"; }
    }

    // 辅助方法：获取 IP
    private String getHostAddress() {
        try { return InetAddress.getLocalHost().getHostAddress(); }
        catch (UnknownHostException e) { return "Unknown"; }
    }
}