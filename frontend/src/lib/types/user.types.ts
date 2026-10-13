export interface UserDto {
    id: number;
    username: string;
}

export interface ChangePasswordRequestDto {
    currentPassword: string;
    newPassword: string;
}
