import { NavLink } from 'react-router-dom'

const tabs = [
  { to: '/admin/products', label: 'Товары' },
  { to: '/admin/categories', label: 'Категории' },
  { to: '/admin/inventory', label: 'Остатки' },
  { to: '/admin/orders', label: 'Заказы' },
  { to: '/admin/promocodes', label: 'Промокоды' },
]

export function AdminTabs() {
  return (
    <nav className="admin-tabs" aria-label="Навигация по разделам админки">
      {tabs.map((tab) => (
        <NavLink
          key={tab.to}
          to={tab.to}
          className={({ isActive }) => ['admin-tab', isActive ? 'active' : ''].join(' ')}
        >
          {tab.label}
        </NavLink>
      ))}
    </nav>
  )
}
