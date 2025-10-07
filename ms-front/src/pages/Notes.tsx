import {Add} from "@mui/icons-material";
import {Box, Button, Card, CardActions, CardContent, CardHeader, Chip, Container, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Stack, TextField, Typography} from "@mui/material";
import {useEffect, useState} from "react";
import {useNavigate, useSearchParams} from "react-router-dom";
import type {Note} from "../data/Note.ts";
import {formatDate} from "../utils/formatDate.ts";
import {toast} from "react-toastify";
import {type ApiError, del, get, post, put} from "../lib/apiCall.ts";
import Loader from "../components/Loader.tsx";

function isApiError(value: unknown): value is ApiError {
    return typeof value === 'object' && value !== null && 'success' in value && (value as { success?: unknown }).success === false;
}

export default function Notes() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const firstName = searchParams.get('firstName');
    const lastName = searchParams.get('lastName');
    const [notes, setNotes] = useState<Note[]>([]);
    const [risk, setRisk] = useState("None")
    const [isLoading, setIsLoading] = useState(false);

    // Dialog states
    const [createOpen, setCreateOpen] = useState(false);
    const [createContent, setCreateContent] = useState("");

    const [editOpen, setEditOpen] = useState(false);
    const [editTarget, setEditTarget] = useState<{ id: string; current: string } | null>(null);
    const [editContent, setEditContent] = useState("");

    const [deleteOpen, setDeleteOpen] = useState(false);
    const [deleteTargetId, setDeleteTargetId] = useState<string | null>(null);

    async function fetchNotes() {
        if (!firstName || !lastName) return;
        setIsLoading(true);
        try {
            const res = await get<Note[]>("/notes", {firstName, lastName});
            if (isApiError(res)) {
                toast.error(res.message || 'Failed to fetch notes');
                setNotes([]);
                return;
            }
            const list = Array.isArray(res) ? res : [];
            const mapped: Note[] = list.map(n => ({
                id: n.id,
                note: n.note,
                createdAt: new Date(n.createdAt),
                updatedAt: new Date(n.updatedAt),
            }));
            setNotes(mapped);
            const riskRes = await get<string>("/risk", {firstName, lastName});
            if (isApiError(riskRes)) {
                toast.error(riskRes.message || 'Failed to fetch risk');
                setRisk("None");
                return;
            }
            setRisk(riskRes);
        } catch (error: unknown) {
            const msg = error instanceof Error ? error.message : 'Failed to fetch notes';
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    }

    async function createNoteApi(content: string) {
        if (!firstName || !lastName) return;
        if (!content) return;
        setIsLoading(true);
        try {
            const res = await post<unknown>("/notes", {firstName, lastName, note: content});
            if (isApiError(res)) {
                const base = res.message || 'Failed to create note';
                toast.error(base);
                return;
            }
            toast.success('Note created');
            await fetchNotes();
        } catch (error: unknown) {
            const msg = error instanceof Error ? error.message : 'Failed to create note';
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    }

    async function updateNoteApi(id: string, content: string) {
        if (content == null) return;
        setIsLoading(true);
        try {
            const res = await put<unknown>("/notes", {note: content}, {id});
            if (isApiError(res)) {
                const base = res.message || 'Failed to update note';
                toast.error(base);
                return;
            }
            toast.success('Note updated');
            await fetchNotes();
        } catch (error: unknown) {
            const msg = error instanceof Error ? error.message : 'Failed to update note';
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    }

    async function deleteNoteApi(id: string) {
        setIsLoading(true);
        try {
            const res = await del<unknown>("/notes", {id});
            if (isApiError(res)) {
                const base = res.message || 'Failed to delete note';
                toast.error(base);
                return;
            }
            toast.success('Note deleted');
            await fetchNotes();
        } catch (error: unknown) {
            const msg = error instanceof Error ? error.message : 'Failed to delete note';
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        if (!firstName || !lastName) {
            navigate('/patients');
            return;
        }
        void fetchNotes();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [firstName, lastName, navigate]);

    let riskColor: "error" | "primary" | "secondary" | "info" | "success" | "warning" | "default";
    switch (risk) {
        case "In Danger":
            riskColor = "error";
            break;
        case "Early onset":
            riskColor = "warning";
            break;
        case "Borderline":
            riskColor = "info";
            break;
        default:
            riskColor = "success";
    }

    if (isLoading) {
        return (
            <Loader/>
        )
    }
    return (
        <Container>
            <Box sx={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem'}}>
                <Stack direction="row" spacing={2} alignItems="center">
                    <Typography variant="h4">{`Notes for ${lastName} ${firstName}`}</Typography>
                    <Chip label={risk} color={riskColor}/>
                </Stack>
                <Button startIcon={<Add/>} onClick={() => setCreateOpen(true)} disabled={isLoading}>Add a note</Button>
            </Box>
            <Grid container spacing={2}>
                {notes.map((n, idx) => (
                    <Grid key={idx} size={{xs: 12, md: 6, lg: 4}}>
                        <NoteCard
                            note={n}
                            onEdit={(note) => {
                                setEditTarget({id: note.id, current: note.note});
                                setEditContent(note.note);
                                setEditOpen(true);
                            }}
                            onDelete={(note) => {
                                setDeleteTargetId(note.id);
                                setDeleteOpen(true);
                            }}
                            disabled={isLoading}
                        />
                    </Grid>
                ))}
                {notes.length === 0 && !isLoading && (
                    <Typography variant="body2">No notes found.</Typography>
                )}
            </Grid>

            {/* Create Note Dialog */}
            <Dialog open={createOpen} onClose={() => setCreateOpen(false)} fullWidth maxWidth="sm">
                <DialogTitle>Create a note</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        id="create-note"
                        label="Note content"
                        type="text"
                        fullWidth
                        multiline
                        minRows={3}
                        value={createContent}
                        onChange={(e) => setCreateContent(e.target.value)}
                        disabled={isLoading}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => {
                        setCreateOpen(false);
                        setCreateContent("");
                    }} disabled={isLoading}>Cancel</Button>
                    <Button onClick={async () => {
                        await createNoteApi(createContent.trim());
                        setCreateOpen(false);
                        setCreateContent("");
                    }} disabled={isLoading || !createContent.trim()}>Save</Button>
                </DialogActions>
            </Dialog>

            {/* Edit Note Dialog */}
            <Dialog open={editOpen} onClose={() => setEditOpen(false)} fullWidth maxWidth="sm">
                <DialogTitle>Edit note</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        id="edit-note"
                        label="Note content"
                        type="text"
                        fullWidth
                        multiline
                        minRows={3}
                        value={editContent}
                        onChange={(e) => setEditContent(e.target.value)}
                        disabled={isLoading}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => {
                        setEditOpen(false);
                        setEditTarget(null);
                    }} disabled={isLoading}>Cancel</Button>
                    <Button onClick={async () => {
                        if (editTarget) {
                            await updateNoteApi(editTarget.id, editContent.trim());
                        }
                        setEditOpen(false);
                        setEditTarget(null);
                    }} disabled={isLoading || (editTarget != null && editContent.trim() === editTarget.current.trim()) || !editContent.trim()}>Save</Button>
                </DialogActions>
            </Dialog>

            {/* Delete Confirm Dialog */}
            <Dialog open={deleteOpen} onClose={() => setDeleteOpen(false)} maxWidth="xs" fullWidth>
                <DialogTitle>Delete note</DialogTitle>
                <DialogContent>
                    <Typography variant="body2">Delete this note? This action cannot be undone.</Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => {
                        setDeleteOpen(false);
                        setDeleteTargetId(null);
                    }} disabled={isLoading}>Cancel</Button>
                    <Button color="error" onClick={async () => {
                        if (deleteTargetId) {
                            await deleteNoteApi(deleteTargetId);
                        }
                        setDeleteOpen(false);
                        setDeleteTargetId(null);
                    }} disabled={isLoading}>Delete</Button>
                </DialogActions>
            </Dialog>
        </Container>
    )
}

function NoteCard({note, onEdit, onDelete, disabled}: { note: Note; onEdit: (note: Note) => void; onDelete: (note: Note) => void; disabled?: boolean }) {
    return (
        <Card sx={{backgroundColor: '#bababa'}}>
            <CardHeader subheader={`Created ${formatDate(note.createdAt)} • Modified ${formatDate(note.updatedAt)}`}/>
            <CardContent>
                <Typography variant="body1">
                    {note.note}
                </Typography>
            </CardContent>
            <CardActions>
                <Button onClick={() => onEdit(note)} disabled={disabled}>Edit</Button>
                <Button color="error" onClick={() => onDelete(note)} disabled={disabled}>Delete</Button>
            </CardActions>
        </Card>
    )
}