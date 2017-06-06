package com.girl.service.impl;

import com.girl.common.Const;
import com.girl.common.ServerResponse;
import com.girl.common.TokenCache;
import com.girl.dao.UserMapper;
import com.girl.pojo.User;
import com.girl.service.IUserService;
import com.girl.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Created by girl on 2017/5/30.
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    public ServerResponse<String> register(User user) {
        // 校验用户名是否存在
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if (! validResponse.isSuccess()) {
            return validResponse;
        }

        // 验证邮箱是否已经被注册
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (! validResponse.isSuccess()) {
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);// 设置为普通用户

        // MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        // 注册
        int resultCount = this.userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @return
     */
    public ServerResponse<User> login(String username, String password) {
        // 校验用户名是否存在
        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {// 用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        // 密码登录MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);

        // 检验密码是否正确
        User user = this.userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);// 将密码置为空
        return ServerResponse.createBySuccess("登录成功", user);
    }

    /**
     * 验证用户名和email是否存在
     *
     * @param str：传入的值
     * @param type：类型
     * @return
     */
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {// 判断是否为空，空格也为空
            if (Const.USERNAME.equals(type)) {
                int resultCount = this.userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已经存在");
                }
            }

            if (Const.EMAIL.equals(type)) {
                int resultCount = this.userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已经被注册");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 根据用户名获取该用户重置密码的问题
     *
     * @param username
     * @return
     */
    public ServerResponse<String> selectQuestion(String username) {
        // 验证用户名是否存在
        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {// 用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        // 通过用户名获取密码提示问题
        String question = this.userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }

        return ServerResponse.createByErrorMessage("找回密码的问题为空");
    }

    /**
     * 验证用户的密码提示问题的答案是否正确
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        // 验证密码提示问题的答案是否正确
        int resultCount = this.userMapper.checkAnswer(username, question, answer);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("答案错误");
        }

        // 生成token
        String forgetToken = UUID.randomUUID().toString();
        // 将token放入guava的本地缓存中
        TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);

        return ServerResponse.createBySuccess(forgetToken);
    }

    /**
     * 忘记密码下的重置密码
     *
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        // 获取forgetToken
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }

        // 验证用户名是否存在
        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {// 用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        // 从本地缓存中获取token
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        // 根据用户名修改用户密码
        if (StringUtils.equals(token, forgetToken)) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = this.userMapper.updatePasswordByUsername(username, md5Password);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("密码修改成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("密码修改失败");
    }

    /**
     * 登录状态下的重置密码
     *
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    public ServerResponse<String> loginResetPassword(String passwordOld, String passwordNew, User user) {
        // 防止横向越权，要校验一下这个用户的旧密码，一定要指定是这个用户
        int resultCount = this.userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码输入错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码修改成功");
        }

        return ServerResponse.createByErrorMessage("密码修改失败");
    }

    /**
     * 更新个人信息
     *
     * @param user
     * @return
     */
    public ServerResponse<User> updateInformation(User user) {
        // email要进行校验，校验新的email是否已经存在，如果已经存在且是当前用户的email，则跳过校验,否则就说明该email已经存在
        int resultCount = this.userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("邮箱已经存在");
        }

        // username不能被更新
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUsername(user.getUsername());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        // 执行更新操作
        int updateCount = this.userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("个人信息更新成功", updateUser);
        }

        return ServerResponse.createByErrorMessage("个人信息更新失败");
    }

    /**
     * 通过用户ID获取用户的详细信息
     *
     * @param userId
     * @return
     */
    public ServerResponse<User> getInformation(Integer userId) {
        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 校验是否为管理员登录
     *
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createByError();
    }

}
