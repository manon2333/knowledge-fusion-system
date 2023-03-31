package com.edu.bistu.datacollectproofaudit.pojo;

import javax.persistence.Column;
import javax.persistence.Id;

public class User {
    /**
     * 用户唯一标识
     */
    @Id
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 银行卡号
     */
    private String bank_card_number;

    /**
     * 银行卡开户行
     */
    private String bank_of_deposit;

    /**
     * 身份证号
     */
    private String id_number;

    /**
     * 手机号码
     */
    private String phone_number;

    /**
     * 学生卡照片
     */
    private String student_card_picture;
    /**
     * 获取用户唯一标识
     *
     * @return id - 用户唯一标识
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置用户唯一标识
     *
     * @param id 用户唯一标识
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取用户名
     *
     * @return username - 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     *
     * @return password - 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }



    /*获取银行卡号
     * @return bank_card_number 银行卡号：
     */
    public String getBank_card_number() {
        return bank_card_number;
    }
    /*
     * @param bank_card_number 银行卡号：
     */
    public void setBank_card_number(String bank_card_number) {
        this.bank_card_number = bank_card_number;
    }

    /*获取银行卡开户行
     * @return bank_of_deposit 银行卡开户行：
     */
    public String getBank_of_deposit() {
        return bank_of_deposit;
    }
    /*
     * @param bank_card_number 银行卡号：
     */
    public void setBank_of_deposit(String bank_of_deposit) {
        this.bank_of_deposit = bank_of_deposit;
    }

    /*获取身份证号
     * @return id_number  身份证号：
     */
    public String getId_number() {
        return id_number;
    }
    /*
     * @param id_number  身份证号：
     */
    public void setId_number(String id_number) {
        this.id_number = id_number;
    }

    /*获取手机号
     * @return phone_number  手机号：
     */
    public String getPhone_number() {
        return phone_number;
    }
    /*
     * @param phone_number  手机号：
     */
    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    /*获取学生卡照片
     * @return student_card_picture  学生卡照片：
     */
    public String getStudent_card_picture() {
        return student_card_picture;
    }
    /*
     * @param student_card_picture  学生卡照片：
     */
    public void setStudent_card_picture(String student_card_picture) {
        this.student_card_picture = student_card_picture;
    }
}