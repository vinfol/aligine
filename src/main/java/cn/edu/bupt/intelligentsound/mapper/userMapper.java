package cn.edu.bupt.intelligentsound.mapper;

import cn.edu.bupt.intelligentsound.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface userMapper {
     User userlogin(@Param("username") String username, @Param("password") String password);
}