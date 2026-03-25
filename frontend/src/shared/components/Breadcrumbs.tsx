import { Link, useLocation, useParams } from 'react-router-dom'

const LABELS: Record<string, string> = {
  cart: 'Корзина',
  checkout: 'Оформление',
  success: 'Успех',
  orders: 'Заказы',
  profile: 'Профиль',
  addresses: 'Адреса',
  admin: 'Админка',
  products: 'Товары',
  categories: 'Категории',
  inventory: 'Остатки',
  promocodes: 'Промокоды',
  login: 'Вход',
  register: 'Регистрация',
}

function resolveLabel(segment: string, productId?: string, orderId?: string) {
  if (segment === productId || segment === orderId) {
    return 'Детали'
  }

  return LABELS[segment] ?? segment
}

export function Breadcrumbs() {
  const location = useLocation()
  const { id: dynamicId } = useParams<{ id?: string }>()

  const segments = location.pathname.split('/').filter(Boolean)
  if (segments.length === 0) {
    return null
  }

  let acc = ''
  const items = segments.map((segment) => {
    acc += `/${segment}`
    return { path: acc, segment }
  })

  return (
    <nav className="breadcrumbs" aria-label="Хлебные крошки">
      <Link to="/">Главная</Link>
      {items.map((item, index) => {
        const isLast = index === items.length - 1
        return (
          <span key={item.path}>
            <span className="breadcrumbs__sep">/</span>
            {isLast ? (
              <span>{resolveLabel(item.segment, dynamicId, dynamicId)}</span>
            ) : (
              <Link to={item.path}>{resolveLabel(item.segment, dynamicId, dynamicId)}</Link>
            )}
          </span>
        )
      })}
    </nav>
  )
}
