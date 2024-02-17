package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserDao userDao;

    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }


    @GetMapping("{userId}")
    public User getUserById(@PathVariable int userId) {
        User user = userDao.getUserById(userId);
        if (user == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found");
        }
        else
        {
            return user;
        }
    }

    @GetMapping
    public List<User> getUsers() { return userDao.getUsers(); }


}
