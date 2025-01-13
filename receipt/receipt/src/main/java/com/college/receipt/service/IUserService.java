package com.college.receipt.service;

import com.college.receipt.entities.UserDto;
import com.college.receipt.exceptions.UserAlreadyExistException;
import com.college.receipt.entities.User;

public interface IUserService {
    User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException;
}

