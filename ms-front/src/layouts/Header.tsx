import {AppBar, Box, Button, Toolbar, Typography} from "@mui/material";
import {useNavigate} from "react-router-dom";

export default function Header() {
    const navItems = ['Home', 'Patients'];
    const navigate = useNavigate();

    return (
        <AppBar position="static" sx={{marginBottom: '1rem'}}>
            <Toolbar>
                <Typography
                    variant="h6"
                    component="div"
                    sx={{flexGrow: 1}}
                >
                    MediLabo
                </Typography>
                <Box sx={{display: {xs: 'none', sm: 'block'}}}>
                    {navItems.map((item) => (
                        <Button key={item} sx={{color: '#fff'}} onClick={(e) => {
                            e.preventDefault()
                            const to = item.toLowerCase() === "home" ? "/" : `/${item.toLowerCase()}`
                            navigate(to)
                        }}>
                            {item}
                        </Button>
                    ))}
                </Box>
            </Toolbar>
        </AppBar>
    )
}