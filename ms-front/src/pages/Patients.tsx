import {Container, IconButton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import {useEffect, useState} from "react";
import {del, get} from "../lib/apiCall.ts";
import {Delete, Edit} from "@mui/icons-material";

export default function Patients() {
    const [patients, setPatients] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    async function fetchPatients() {
        setIsLoading(true);
        try {
            const patient = await get('/patient/all');
            setPatients(patient);
        } catch (error) {
            console.log(error);
        } finally {
            setIsLoading(false);
        }
    }

    async function deletePatient(firstName: string, lastName: string) {
        const patient = await del('/patient', {firstName, lastName});
        if (patient.success) {
            setPatients(patients.filter((p) => p.firstName !== firstName || p.lastName !== lastName));
        }
    }

    useEffect(() => {
        void fetchPatients();
    }, [])
    return (
        <Container>
            <h1>Patients</h1>
            <TableContainer>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>First Name</TableCell>
                            <TableCell>Last Name</TableCell>
                            <TableCell>Birth Date</TableCell>
                            <TableCell>Gender</TableCell>
                            <TableCell>Address</TableCell>
                            <TableCell>Phone Number</TableCell>
                            <TableCell>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {
                            isLoading ? (<>Loading...</>) :
                                patients.map((patient) => (
                                    <TableRow key={`Patient_${patient.firstName}_${patient.lastName}`}>
                                        <TableCell>{patient.firstName}</TableCell>
                                        <TableCell>{patient.lastName}</TableCell>
                                        <TableCell>{new Date(patient.birthDate).toLocaleDateString()}</TableCell>
                                        <TableCell>{patient.gender}</TableCell>
                                        <TableCell>{patient.address}</TableCell>
                                        <TableCell>{patient.phoneNumber}</TableCell>
                                        <TableCell>
                                            <Stack direction="row" spacing={2}>
                                                <IconButton>
                                                    <Edit/>
                                                </IconButton>
                                                <IconButton onClick={(e) => {
                                                    e.preventDefault()
                                                    void deletePatient(patient.firstName, patient.lastName)
                                                }}>
                                                    <Delete/>
                                                </IconButton>
                                            </Stack>
                                        </TableCell>
                                    </TableRow>
                                ))
                        }
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
    )
}