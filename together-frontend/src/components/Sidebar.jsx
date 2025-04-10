import { useState, useEffect } from "react";
import { useAuth } from "../providers/AuthProvider";
import {
    getReceivedRequests,
    acceptRequest,
    rejectRequest,
    getFriends,
    searchUsers,
    sendRequest
} from "../api/friendService";
import default_profile from "../assets/default_profile.png"

const Sidebar = ({ isOpen, onClose }) => {
    const { user } = useAuth();
    const [searchQuery, setSearchQuery] = useState("");
    const [searchResults, setSearchResults] = useState([]);
    const [requests, setRequests] = useState([]);
    const [friends, setFriends] = useState([]);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (isOpen) {
            document.body.classList.add("overflow-hidden");
            fetchData();
        } else {
            document.body.classList.remove("overflow-hidden");
        }

        return () => {
            document.body.classList.remove("overflow-hidden");
        };
    }, [isOpen]);

    const fetchData = async () => {
        setIsLoading(true);
        try {
            const [requestsRes, friendsRes] = await Promise.all([
                getReceivedRequests({ page: 0, size: 10 }),
                getFriends({ page: 0, size: 10 })
            ]);
            setRequests(requestsRes);
            setFriends(friendsRes);
        } catch (error) {
            console.error("Error fetching data:", error);
        }
        setIsLoading(false);
    };

    const handleSearch = async (query) => {
        if (!query) {
            setSearchResults([]);
            return;
        }
        try {
            const results = await searchUsers(query, { page: 0, size: 5 });
            setSearchResults(results.content);
        } catch (error) {
            console.error("Search error:", error);
        }
    };

    const handleRequestAction = async (action, nickname) => {
        try {
            if (action === "accept") {
                await acceptRequest(nickname);
            } else {
                await rejectRequest(nickname);
            }
            setRequests(prev => prev.filter(req => req.nickName !== nickname));
            await fetchData()
        } catch (error) {
            console.error("Action failed:", error);
        }
    };

    const handleSendInvitation = async (user) => {
        try {
            await sendRequest(user.nickName);
            setSearchResults(prev => prev.filter(u => u.id !== user.id));
        } catch (error) {
            const errorMessage = error.response?.data?.message ||
                error.message ||
                "Failed to send invitation";
            alert(errorMessage);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 bg-black bg-opacity-50">
            <div className="absolute right-0 top-0 h-full w-full max-w-md bg-gray-800 text-white shadow-xl">
                <div className="p-4 border-b border-gray-700">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-xl font-bold">Profile</h2>
                        <button onClick={onClose} className="p-2 hover:bg-gray-700 rounded">
                            ✕
                        </button>
                    </div>
                    <div className="flex items-center space-x-4">
                        <div className="w-12 h-12 rounded-full bg-gray-600 flex items-center justify-center">
                            <img src={default_profile} alt="default profile"></img>
                        </div>
                        <div>
                            <p className="font-semibold">{user.nickName}</p>
                            <p className="text-sm text-gray-400">{user.email}</p>
                        </div>
                    </div>
                </div>

                <div className="p-4 border-b border-gray-700">
                    <input
                        type="text"
                        placeholder="Search users..."
                        className="w-full p-2 rounded bg-gray-700 text-white"
                        value={searchQuery}
                        onChange={(e) => {
                            const value = e.target.value;
                            setSearchQuery(value);
                            handleSearch(value);
                        }}
                    />
                    {searchResults.length > 0 && (
                        <div className="mt-2 space-y-2">
                            {searchResults.map(user => (
                                <div key={user.id} className="flex items-center justify-between p-2 hover:bg-gray-700 rounded">
                                    <div className="flex items-center">
                                        <img
                                            src={default_profile}
                                            className="w-10 h-10 rounded-full"
                                            alt={`${user.nickName}'s profile`}
                                        />
                                        <span className="mx-4">{user.nickName}</span>
                                    </div>
                                    <button
                                        onClick={() => handleSendInvitation(user)}
                                        className="px-3 py-1 bg-blue-600 hover:bg-blue-700 rounded text-sm transition-colors"
                                    >
                                        Add Friend
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div className="p-4 border-b border-gray-700">
                    <h3 className="font-semibold mb-2">Friend Requests ({requests.length})</h3>
                    {isLoading ? (
                        <div className="text-gray-400">Loading...</div>
                    ) : (
                        requests.map(request => (
                            <div key={request.id} className="flex items-center justify-between p-2 hover:bg-gray-700 rounded">
                                <div>
                                    <p className="font-medium">{request.nickName}</p>
                                    <p className="text-sm text-gray-400">{request.email}</p>
                                </div>
                                <div className="space-x-2">
                                    <button
                                        onClick={() => handleRequestAction("accept", request.nickName)}
                                        className="px-3 py-1 bg-green-600 rounded hover:bg-green-700"
                                    >
                                        Accept
                                    </button>
                                    <button
                                        onClick={() => handleRequestAction("reject", request.nickName)}
                                        className="px-3 py-1 bg-red-600 rounded hover:bg-red-700"
                                    >
                                        Reject
                                    </button>
                                </div>
                            </div>
                        ))
                    )}
                </div>

                <div className="p-4">
                    <h3 className="font-semibold mb-2">Friends ({friends.length})</h3>
                    {isLoading ? (
                        <div className="text-gray-400">Loading...</div>
                    ) : (
                        friends.map(friend => (
                            <div key={friend.id} className="flex items-center p-2 hover:bg-gray-700 rounded">
                                <div className="w-8 h-8 rounded-full bg-gray-600 flex items-center justify-center mr-2">
                                    <img src={default_profile}></img>
                                </div>
                                <div>
                                    <p className="font-medium">{friend.nickName}</p>
                                    <p className="text-sm text-gray-400">{friend.email}</p>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
};

export default Sidebar;