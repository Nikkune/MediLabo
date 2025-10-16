export interface Patient {
    firstName: string;
    lastName: string;
    birthDate: Date;
    gender: string;
    address: string | null;
    phoneNumber: string | null;
}