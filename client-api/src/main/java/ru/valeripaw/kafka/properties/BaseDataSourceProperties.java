package ru.valeripaw.kafka.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDataSourceProperties {

    protected String url;
    protected String username;
    protected String password;
    protected String driverClassName;

}
