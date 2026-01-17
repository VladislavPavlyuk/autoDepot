import axios from "axios";

const client = axios.create({
  baseURL: "/api",
  timeout: 5000
});

export default client;
