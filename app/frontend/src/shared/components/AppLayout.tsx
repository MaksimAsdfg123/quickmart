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
      <header className="topbar-wrap" data-testid="topbar">
        <div className="topbar">
          <Link to="/" className="brand" data-testid="topbar-brand">
            Быстромаркет
          </Link>
          <nav className="nav-links" aria-label="Навигация">
            <NavLink to="/" data-testid="topbar-nav-catalog">Каталог</NavLink>
            {user && (
              <NavLink to="/cart" className="nav-link-with-badge" data-testid="topbar-nav-cart">
                <span>Корзина</span>
                {cartItemsCount > 0 ? <Badge tone="success">{cartItemsCount}</Badge> : null}
              </NavLink>
            )}
            {user && <NavLink to="/orders" data-testid="topbar-nav-orders">Заказы</NavLink>}
            {user && <NavLink to="/profile/addresses" data-testid="topbar-nav-addresses">Адреса</NavLink>}
            {user?.role === 'ADMIN' && <NavLink to="/admin/products" data-testid="topbar-nav-admin">Админка</NavLink>}
          </nav>
          <div>
            {user ? (
              <div className="session-actions">
                <span
                  className="session-user" data-testid="session-user"
                  title={`${user.fullName} (${user.role === 'ADMIN' ? 'Администратор' : 'Покупатель'})`}
                >
                  {user.fullName} · {user.role === 'ADMIN' ? 'Администратор' : 'Покупатель'}
                </span>
                <Button
                  variant="secondary"
                  size="sm"
                  data-testid="logout-button"
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
                <NavLink to="/login" data-testid="topbar-login-link">Вход</NavLink>
                <NavLink to="/register" data-testid="topbar-register-link">Регистрация</NavLink>
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

