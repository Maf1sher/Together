import {Link, useNavigate} from "react-router-dom";
import {register} from "../api/authService.js";
import {useState} from "react";

function RegisterPage() {

    const [firstname, setFirstname] = useState('');
    const [lastname, setLastname] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const [firstnameError, setFirstnameError] = useState('');
    const [lastnameError, setLastnameError] = useState('');
    const [usernameError, setUsernameError] = useState('');
    const [emailError, setEmailError] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [confirmPasswordError, setConfirmPasswordError] = useState([]);

    const [error, setError] = useState([]);

    const navigate = useNavigate();

    const validateEmail = (email) => {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    };

    const validate = () => {
        let valid = true;

        if (!firstname) {
            setFirstnameError('Firstname is required');
            valid = false;
        } else {
            setFirstnameError('');
        }

        if (!lastname) {
            setLastnameError('Password is required');
            valid = false;
        } else {
            setLastnameError('');
        }

        if (!username) {
            setUsernameError('Password is required');
            valid = false;
        } else {
            setUsernameError('');
        }

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

        setConfirmPasswordError([])

        if (!confirmPassword) {
            setConfirmPasswordError(errors => [...errors,'Confirmation password is required']);
            valid = false;
        }

        if (password !== confirmPassword) {
            setConfirmPasswordError(errors =>[...errors,'Passwords do not match']);
            valid = false;
        }

        return valid;
    }

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validate()) return;

        await register({
            firstName: firstname,
            lastName: lastname,
            nickName: username,
            email: email,
            password: password
        })
            .then((user) => {
                navigate('/login');
            })
            .catch(err => {
                setError(err.response?.data?.validationErrors || "A Register error");
            })
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-900">
            <div className="bg-gray-800 p-8 rounded-2xl shadow-lg w-96">
                <h2 className="text-white text-2xl font-semibold text-center mb-6">Sign Up</h2>

                {error && error.length > 0 && (
                    <div className="bg-red-600 text-white text-sm rounded-md p-3 mb-4 shadow-md">
                        <ul className="list-disc list-inside text-left">
                            {error.map((err, index) => (
                                <li key={index}>{err}</li>
                            ))}
                        </ul>
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-400 text-sm mb-2">First Name</label>
                        <input
                            type="text"
                            className="w-full p-3 bg-gray-700 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Enter your first name"
                            value={firstname}
                            onChange={(e) => setFirstname(e.target.value)}
                        />
                        {firstnameError && (
                            <p className="text-red-400 text-sm mt-1">{firstnameError}</p>
                        )}
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-400 text-sm mb-2">Last Name</label>
                        <input
                            type="text"
                            className="w-full p-3 bg-gray-700 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Enter your last name"
                            value={lastname}
                            onChange={(e) => setLastname(e.target.value)}
                        />
                        {lastnameError && (
                            <p className="text-red-400 text-sm mt-1">{lastnameError}</p>
                        )}
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-400 text-sm mb-2">Username</label>
                        <input
                            type="text"
                            className="w-full p-3 bg-gray-700 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Enter your username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                        {usernameError && (
                            <p className="text-red-400 text-sm mt-1">{usernameError}</p>
                        )}
                    </div>
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
                    <div className="mb-4">
                        <label className="block text-gray-400 text-sm mb-2">Confirm Password</label>
                        <input
                            type="password"
                            className="w-full p-3 bg-gray-700 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Confirm your password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                        />
                        {confirmPasswordError && confirmPasswordError.length > 0 && (
                            <div>
                                {confirmPasswordError.map((err, index) => (
                                    <p className="text-red-400 text-sm mt-1">{err}</p>
                                ))}
                            </div>
                        )}
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold p-3 rounded-lg transition duration-300">
                        Sign Up
                    </button>
                </form>
                <p className="text-gray-400 text-sm text-center mt-4">
                    Already have an account?
                    <Link
                        to={"/login"}
                        className="text-blue-500 hover:underline"
                    >
                        Log in
                    </Link>
                </p>
            </div>
        </div>
    );
}

export default RegisterPage;
