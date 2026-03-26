import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  const authRaw = localStorage.getItem('quickmart-auth')
  if (authRaw) {
    try {
      const parsed = JSON.parse(authRaw) as { state?: { token?: string | null } }
      const token = parsed.state?.token
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch {
      // ignore malformed local storage
    }
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      localStorage.removeItem('quickmart-auth')
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        const from = `${window.location.pathname}${window.location.search}`
        const params = new URLSearchParams({
          reason: 'session-expired',
          from,
        })
        window.location.href = `/login?${params.toString()}`
      }
    }
    return Promise.reject(error)
  },
)
