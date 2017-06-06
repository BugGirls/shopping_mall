package com.girl.service;

import com.girl.common.ServerResponse;
import com.girl.pojo.User;

/**
 * Created by girl on 2017/5/30.
 */
public interface IUserService {

    ServerResponse<String> register(User user);

    ServerResponse<User> login(String username, String password);

    ServerResponse<String> checkValid(String str, String type);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> loginResetPassword(String passwordOld, String passwordNew, User user);

    ServerResponse<User> updateInformation(User user);

    ServerResponse<User> getInformation(Integer userId);

    ServerResponse<String> checkAdminRole(User user);

}
