import {Container} from "@mui/material";
import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {del, get, post, put, type ApiError} from "../lib/apiCall.ts";
import {Add, Close, Delete, Edit, Save, Note} from "@mui/icons-material";
import {toast} from "react-toastify";
import type {Patient} from "../data/Patient.ts";
import {DataGrid, GridActionsCellItem, GridRowEditStopReasons, type GridRowId, GridRowModes, type GridRowModesModel, Toolbar, ToolbarButton, type GridRowParams} from "@mui/x-data-grid";

type PatientDTO = Omit<Patient, 'birthDate'> & { birthDate: string | null };
type PatientRow = Omit<Patient, 'birthDate'> & { id: GridRowId; isNew?: boolean; birthDate: Date | null };

function isApiError(value: unknown): value is ApiError {
    return typeof value === 'object' && value !== null && 'success' in value && (value as { success?: unknown }).success === false;
}

const EditToolbar = ({setPatients, setRowModesModel}: { setPatients: React.Dispatch<React.SetStateAction<PatientRow[]>>; setRowModesModel: React.Dispatch<React.SetStateAction<GridRowModesModel>>; }) => {
    const handleClick = () => {
        const id = `new-${Date.now()}`;
        setPatients((oldRows) => [
            {id, lastName: '', firstName: '', birthDate: null, gender: 'M', address: '', phoneNumber: '', isNew: true},
            ...oldRows,
        ]);
        setRowModesModel((oldModel) => ({...oldModel, [id]: {mode: GridRowModes.Edit, fieldToFocus: 'lastName'}}));
    };
    return (
        <Toolbar>
            <ToolbarButton onClick={handleClick}>
                <Add/>
            </ToolbarButton>
        </Toolbar>
    );
};

export default function Patients() {
    const [patients, setPatients] = useState<PatientRow[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({});
    const navigate = useNavigate();

    async function fetchPatients() {
        setIsLoading(true);
        try {
            const patient = await get<PatientDTO[]>("/patient/all");
            if (isApiError(patient)) {
                toast.error(patient.message || 'Failed to fetch patients');
                setPatients([]);
                return;
            }
            const list: PatientDTO[] = Array.isArray(patient) ? patient : [];
            const rows: PatientRow[] = list.map((p, index) => ({
                id: index,
                firstName: p.firstName,
                lastName: p.lastName,
                birthDate: p.birthDate ? new Date(p.birthDate) : null,
                gender: p.gender,
                address: p.address,
                phoneNumber: p.phoneNumber,
            }));
            setPatients(rows);
        } catch (error: unknown) {
            const msg = error instanceof Error ? error.message : 'Failed to fetch patients';
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    }

    async function deletePatient(firstName: string, lastName: string) {
        try {
            const res = await del<unknown>('/patient', {firstName, lastName});
            if (isApiError(res)) {
                const errs = res.errors;
                const errText = errs ? Object.values(errs).join(', ') : res.error ?? undefined;
                const base = res.message || 'Failed to delete patient';
                toast.error(errText ? `${base}: ${errText}` : base);
                return;
            }
            toast.success('Patient deleted successfully');
            await fetchPatients();
        } catch (error: unknown) {
            const msg = error instanceof Error ? error.message : 'Failed to delete patient';
            toast.error(msg);
        }
    }

    const handleRowEditStop = (params: { reason?: unknown }, event: { defaultMuiPrevented?: boolean }) => {
        if ((params as { reason?: string }).reason === GridRowEditStopReasons.rowFocusOut) {
            event.defaultMuiPrevented = true;
        }
    };

    const handleEditClick = (id: GridRowId) => () => {
        setRowModesModel((prev) => ({...prev, [id]: {mode: GridRowModes.Edit}}));
    };

    const handleSaveClick = (id: GridRowId) => () => {
        setRowModesModel((prev) => ({...prev, [id]: {mode: GridRowModes.View}}));
    };

    const handleCancelClick = (id: GridRowId) => () => {
        setRowModesModel((prev) => ({...prev, [id]: {mode: GridRowModes.View, ignoreModifications: true}}));
        setPatients((prev) => prev.map((row) => (row.id === id ? {...row, isNew: false} : row)));
    };

    const processRowUpdate = async (newRow: PatientRow, oldRow: PatientRow): Promise<PatientRow> => {
        const payload: PatientDTO = {
            firstName: newRow.firstName,
            lastName: newRow.lastName,
            birthDate: newRow.birthDate ? newRow.birthDate.toISOString() : null,
            gender: newRow.gender,
            address: newRow.address,
            phoneNumber: newRow.phoneNumber,
        };

        const isCreate = !!oldRow?.isNew;
        const res = isCreate ? await post<PatientDTO>('/patient', payload) : await put<PatientDTO>('/patient', payload);
        if (isApiError(res)) {
            const base = res.message || (isCreate ? 'Failed to create patient' : 'Failed to update patient');
            toast.error(base);
            throw new Error(base);
        }
        toast.success(isCreate ? 'Patient created successfully' : 'Patient updated successfully');
        const updatedRow: PatientRow = {...newRow, isNew: false};
        setPatients((prev) => prev.map((row) => (row.id === newRow.id ? updatedRow : row)));
        return updatedRow;
    };

    useEffect(() => {
        void fetchPatients();
    }, [])

    return (
        <Container>
            <h1>Patients</h1>
            <DataGrid<PatientRow>
                columns={[
                    {field: "lastName", headerName: "Last Name", flex: 1, editable: true},
                    {field: "firstName", headerName: "First Name", flex: 1, editable: true},
                    {
                        field: "birthDate",
                        headerName: "Birth Date",
                        type: 'date',
                        flex: 1,
                        editable: true,
                        valueFormatter: (value: unknown) => {
                            if (value == null) {
                                return '';
                            }
                            try {
                                const d = value instanceof Date ? value : new Date(String(value));
                                return d.toLocaleDateString();
                            } catch {
                                return '';
                            }
                        }
                    },
                    {field: "gender", headerName: "Gender", type: 'singleSelect', flex: 1, valueOptions: ["M", "F"], editable: true},
                    {field: "address", headerName: "Address", flex: 1, editable: true},
                    {field: "phoneNumber", headerName: "Phone Number", flex: 1, editable: true},
                    {
                        field: 'actions',
                        headerName: 'Actions',
                        type: 'actions',
                        getActions: (params: GridRowParams<PatientRow>) => {
                            const {id} = params;
                            const isInEditMode = rowModesModel[id]?.mode === GridRowModes.Edit;

                            if (isInEditMode) {
                                return [
                                    <GridActionsCellItem onClick={handleSaveClick(id)} icon={<Save/>} label="Save" showInMenu/>,
                                    <GridActionsCellItem onClick={handleCancelClick(id)} icon={<Close/>} label="Cancel"/>,
                                ];
                            }

                            return [
                                <GridActionsCellItem icon={<Note/>} onClick={() => navigate(`/notes?firstName=${params.row.firstName}&lastName=${params.row.lastName}`)} label="Notes" showInMenu/>,
                                <GridActionsCellItem onClick={handleEditClick(id)} icon={<Edit/>} label="Edit" showInMenu/>,
                                <GridActionsCellItem icon={<Delete/>} onClick={() => void deletePatient(params.row.firstName, params.row.lastName)} label="Delete" showInMenu/>,
                            ]
                        }
                    },
                ]}
                editMode="row"
                rowModesModel={rowModesModel}
                onRowModesModelChange={(newModel) => setRowModesModel(newModel)}
                onRowEditStop={handleRowEditStop}
                processRowUpdate={processRowUpdate}
                rows={patients}
                loading={isLoading}
                slots={{toolbar: EditToolbar}}
                slotProps={{toolbar: {setPatients, setRowModesModel}}}
                showToolbar
            />
        </Container>
    )
}