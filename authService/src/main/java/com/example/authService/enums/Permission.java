package com.example.authService.enums;

public enum Permission {
    // Quyền quản lý tài khoản và người dùng (Manager)
    CREATE_ACCOUNT,
    MANAGE_USER,

    // Quyền quản lý dự án (Manager, Leader)
    MANAGE_PROJECT,

    // Quyền quản lý task (Manager, Leader)
    MANAGE_TASK,
    ASSIGN_TASK,

    // Quyền thao tác với task cá nhân (Manager, Leader, Employee)
    VIEW_TASK,
    UPDATE_TASK_STATUS,
    SUBMIT_TASK
}
