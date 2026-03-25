import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, NavLink, Outlet } from 'react-router-dom'

import { cartApi } from '../../api/cartApi'
import { useAuthStore } from '../lib/authStore'
import { Badge } from './ui/Badge'
import { Button } from './ui/Button'

export function AppLayout() {
  const queryClient = useQueryClient()
  const { user, logout } = useAuthStore()

  const cartSummaryQuery = useQuery({
    queryKey: ['cart'],
    queryFn: cartApi.get,
    enabled: Boolean(user),
    staleTime: 1000 * 20,
  })

  const cartItemsCount = cartSummaryQuery.data?.totalItems ?? 0

  return (
    <div className="app-shell">
      <header className="topbar-wrap">
        <div className="topbar">
          <Link to="/" className="brand">
            Быстромаркет
          </Link>
          <nav className="nav-links" aria-label="Навигация">
            <NavLink to="/">Каталог</NavLink>
            {user && (
              <NavLink to="/cart" className="nav-link-with-badge">
                <span>Корзина</span>
                {cartItemsCount > 0 ? <Badge tone="success">{cartItemsCount}</Badge> : null}
              </NavLink>
            )}
            {user && <NavLink to="/orders">Заказы</NavLink>}
            {user && <NavLink to="/profile/addresses">Адреса</NavLink>}
            {user?.role === 'ADMIN' && <NavLink to="/admin/products">Админка</NavLink>}
          </nav>
          <div>
            {user ? (
              <div className="session-actions">
                <span
                  className="session-user"
                  title={`${user.fullName} (${user.role === 'ADMIN' ? 'Администратор' : 'Покупатель'})`}
                >
                  {user.fullName} · {user.role === 'ADMIN' ? 'Администратор' : 'Покупатель'}
                </span>
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => {
                    logout()
                    queryClient.clear()
                  }}
                >
                  Выйти
                </Button>
              </div>
            ) : (
              <div className="auth-links">
                <NavLink to="/login">Вход</NavLink>
                <NavLink to="/register">Регистрация</NavLink>
              </div>
            )}
          </div>
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
