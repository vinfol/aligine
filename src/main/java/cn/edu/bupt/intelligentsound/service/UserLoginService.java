package cn.edu.bupt.intelligentsound.service;

import cn.edu.bupt.intelligentsound.entity.User;
import cn.edu.bupt.intelligentsound.mapper.userMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserLoginService {

    @Autowired
    private userMapper usermapper;

    public User userLogin(String username,String password){
        User user = usermapper.userlogin(username,password);
        return user;
    }
}
