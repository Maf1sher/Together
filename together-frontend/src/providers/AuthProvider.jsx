import { createContext, useContext, useEffect, useState } from 'react';
import {checkAuthStatus} from "../api/authService.js";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [user, setUser] = useState(null);
    const [isLoading, setLoading] = useState(true);

    const authStatus = async () => {

        try {
            let response = await checkAuthStatus()
            if (response != null) {
                setIsAuthenticated(true);
                setUser(response);
            } else {
                setIsAuthenticated(false);
                setUser(null);
            }
        } catch (error) {
            setIsAuthenticated(false);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        authStatus()
    }, []);

    const value = {
        isAuthenticated,
        isLoading,
        user,
        setUser: (user) => setUser(user),
        setAuthStatus: (status) => setIsAuthenticated(status),
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
