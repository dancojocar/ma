package com.example.ma.sm.model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
  private long id;
  private String username;
  private String pass;
  private String token;
  private List<Portfolio> portfolios;

  public User(String username, String pass) {
    this.username = username;
    this.pass = pass;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPass() {
    return pass;
  }

  public void setPass(String pass) {
    this.pass = pass;
  }

  public List<Portfolio> getPortfolios() {
    return portfolios;
  }

  public void setPortfolios(List<Portfolio> portfolios) {
    this.portfolios = portfolios;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
