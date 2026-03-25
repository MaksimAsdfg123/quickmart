import { createBrowserRouter } from 'react-router-dom'

import { AddressesPage } from '../pages/AddressesPage'
import { AdminCategoriesPage } from '../pages/admin/AdminCategoriesPage'
import { AdminInventoryPage } from '../pages/admin/AdminInventoryPage'
import { AdminOrdersPage } from '../pages/admin/AdminOrdersPage'
import { AdminProductsPage } from '../pages/admin/AdminProductsPage'
import { AdminPromocodesPage } from '../pages/admin/AdminPromocodesPage'
import { CartPage } from '../pages/CartPage'
import { CatalogPage } from '../pages/CatalogPage'
import { CheckoutPage } from '../pages/CheckoutPage'
import { CheckoutSuccessPage } from '../pages/CheckoutSuccessPage'
import { LoginPage } from '../pages/LoginPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { OrderDetailsPage } from '../pages/OrderDetailsPage'
import { OrderHistoryPage } from '../pages/OrderHistoryPage'
import { ProductDetailsPage } from '../pages/ProductDetailsPage'
import { RegisterPage } from '../pages/RegisterPage'
import { AdminRoute } from '../shared/components/AdminRoute'
import { AppLayout } from '../shared/components/AppLayout'
import { ProtectedRoute } from '../shared/components/ProtectedRoute'

export const appRouter = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <CatalogPage /> },
      { path: 'products/:id', element: <ProductDetailsPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      {
        path: 'cart',
        element: (
          <ProtectedRoute>
            <CartPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'checkout',
        element: (
          <ProtectedRoute>
            <CheckoutPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'checkout/success/:id',
        element: (
          <ProtectedRoute>
            <CheckoutSuccessPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'orders',
        element: (
          <ProtectedRoute>
            <OrderHistoryPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'orders/:id',
        element: (
          <ProtectedRoute>
            <OrderDetailsPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'profile/addresses',
        element: (
          <ProtectedRoute>
            <AddressesPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'admin/products',
        element: (
          <AdminRoute>
            <AdminProductsPage />
          </AdminRoute>
        ),
      },
      {
        path: 'admin/categories',
        element: (
          <AdminRoute>
            <AdminCategoriesPage />
          </AdminRoute>
        ),
      },
      {
        path: 'admin/inventory',
        element: (
          <AdminRoute>
            <AdminInventoryPage />
          </AdminRoute>
        ),
      },
      {
        path: 'admin/orders',
        element: (
          <AdminRoute>
            <AdminOrdersPage />
          </AdminRoute>
        ),
      },
      {
        path: 'admin/promocodes',
        element: (
          <AdminRoute>
            <AdminPromocodesPage />
          </AdminRoute>
        ),
      },
      {
        path: '*',
        element: <NotFoundPage />,
      },
    ],
  },
])
