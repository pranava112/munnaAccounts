package com.vpm.Accounts.DTO.AI;

public class AIAccountDTO {

    private Long id;

    private String name;

    private String code;

    private String type;

    private String accountGroup;

    public AIAccountDTO() {
    }

    public AIAccountDTO(
            Long id,
            String name,
            String code,
            String type,
            String accountGroup) {

        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
        this.accountGroup = accountGroup;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getAccountGroup() {
        return accountGroup;
    }
}