package com.edu.bistu.datacollectproofaudit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * StringRedisTemplate与RedisTemplate区别点
     * 两者的关系是StringRedisTemplate继承RedisTemplate。
     *
     * 两者的数据是不共通的；也就是说StringRedisTemplate只能管理StringRedisTemplate里面的数据，RedisTemplate只能管理RedisTemplate中的数据。
     *
     * 其实他们两者之间的区别主要在于他们使用的序列化类:
     * 　　　　RedisTemplate使用的是JdkSerializationRedisSerializer    存入数据会将数据先序列化成字节数组然后在存入Redis数据库。
     *
     * 　　 　  StringRedisTemplate使用的是StringRedisSerializer
     *
     * 使用时注意事项：
     * 　　　当你的redis数据库里面本来存的是字符串数据或者你要存取的数据就是字符串类型数据的时候，那么你就使用StringRedisTemplate即可。
     * 　　　但是如果你的数据是复杂的对象类型，而取出的时候又不想做任何的数据转换，直接从Redis里面取出一个对象，那么使用RedisTemplate是更好的选择。
     */

    @Bean      //用GenericJackson2JsonRedisSerializer来序列化
    public RedisTemplate redisTemplate(LettuceConnectionFactory lettuceConnectionFactory){
        //我们为了自己开发方便，一般直接使用<String,Object>
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 设置键（key）的序列化采用StringRedisSerializer
        StringRedisSerializer stringRedisSerializer=new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        // 设置值（value）的序列化采用GenericJackson2JsonRedisSerializer
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        redisTemplate.setValueSerializer(jsonRedisSerializer);

        //设置连接工厂
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }


//    @Bean      //用Jackson2JsonRedisSerializer来序列化
//    public RedisTemplate<String,Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
//        //我们为了自己开发方便，一般直接使用<String,Object>
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//
//        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
//        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        //mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);   过期了
//        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance ,
//                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
//
//        serializer.setObjectMapper(mapper);
//
//        redisTemplate.setValueSerializer(serializer);
//        //使用StringRedisSerializer来序列化和反序列化redis的key值
//        StringRedisSerializer stringRedisSerializer=new StringRedisSerializer();
//        redisTemplate.setKeySerializer(stringRedisSerializer);
//        redisTemplate.setHashKeySerializer(stringRedisSerializer);
//
//        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
//
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }

//Jackson2JsonRedisSerializer和GenericJackson2JsonRedisSerializer的区别     https://blog.csdn.net/bai_bug/article/details/81222519

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(factory);
        return stringRedisTemplate;
    }


}
