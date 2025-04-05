import {Link, useNavigate} from "react-router-dom";
import { useState } from "react";
import { login } from "../api/authService.js";
import {useAuth} from "../providers/AuthProvider.jsx";

function LoginPage() {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [emailError, setEmailError] = useState('');
    const [passwordError, setPasswordError] = useState('');

    const {setAuthStatus, setUser} = useAuth()
    const navigate = useNavigate();

    const validateEmail = (email) => {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    };


    const handleSubmit = async (e) => {
        e.preventDefault();

        let valid = true;

        if (!validateEmail(email)) {
            setEmailError('Invalid email');
            valid = false;
        } else {
            setEmailError('');
        }

        if (!password) {
            setPasswordError('Password is required');
            valid = false;
        } else {
            setPasswordError('');
        }

        if (!valid) return;

        await login({ email, password })
            .then((user) => {
                setAuthStatus(true);
                setUser(user);
                navigate('/');
            })
            .catch(err => setError(err.response?.data?.error || "A login error"))
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-900">
            <div className="bg-gray-800 p-8 rounded-2xl shadow-lg w-96">
                <h2 className="text-white text-2xl font-semibold text-center mb-6">Log In</h2>

                {error && (
                    <div className="bg-red-600 text-white text-sm rounded-md p-3 mb-4 shadow-md text-center">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-400 text-sm mb-2">Email</label>
                        <input
                            type="text"
                            className="w-full p-3 bg-gray-700 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Enter your email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />
                        {emailError && (
                            <p className="text-red-400 text-sm mt-1">{emailError}</p>
                        )}
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-400 text-sm mb-2">Password</label>
                        <input
                            type="password"
                            className="w-full p-3 bg-gray-700 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Enter your password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        {passwordError && (
                            <p className="text-red-400 text-sm mt-1">{passwordError}</p>
                        )}
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold p-3 rounded-lg transition duration-300">
                        Log In
                    </button>
                </form>
                <p className="text-gray-400 text-sm text-center mt-4">
                    Don't have an account?{" "}
                    <Link
                        to={"/register"}
                        className="text-blue-500 hover:underline"
                    >
                        Sign Up
                    </Link>
                </p>
            </div>
        </div>
    );
}

export default LoginPage;
