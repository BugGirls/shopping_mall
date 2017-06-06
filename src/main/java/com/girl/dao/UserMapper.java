package com.girl.dao;

import com.girl.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    // 验证有户名是否存在
    int checkUsername(String username);

    // 通过登录信息获取用户信息
    User selectLogin(@Param(value = "username") String username, @Param(value = "password") String password);

    // 验证邮箱是否已经被注册
    int checkEmail(String email);

    // 通过用户名获取该用户的密码提示问题
    String selectQuestionByUsername(String username);

    // 验证用户名的密码提示问题的答案是否正确
    int checkAnswer(@Param(value = "username") String username, @Param(value = "question") String question, @Param(value = "answer") String answer);

    // 通过用户名更新用户密码
    int updatePasswordByUsername(@Param(value = "username") String username, @Param(value = "passwordNew") String passwordNew);

    // 验证密码是否正确
    int checkPassword(@Param(value = "password") String password, @Param(value = "userId") Integer userId);

    // 验证邮箱是否存在，不验证当前用户的email
    int checkEmailByUserId(@Param(value = "email") String email, @Param(value = "userId") Integer userId);
}