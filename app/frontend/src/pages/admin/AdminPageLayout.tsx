import type { PropsWithChildren, ReactNode } from 'react'

import { AdminTabs } from './AdminTabs'

interface AdminPageLayoutProps {
  title: string
  subtitle: string
  actions?: ReactNode
}

export function AdminPageLayout({ title, subtitle, actions, children }: PropsWithChildren<AdminPageLayoutProps>) {
  return (
    <div className="page admin-page">
      <AdminTabs />

      <section className="page-head">
        <div>
          <h1 className="page-title">{title}</h1>
          <p className="page-subtitle">{subtitle}</p>
        </div>
        {actions ? <div className="page-head__actions">{actions}</div> : null}
      </section>

      {children}
    </div>
  )
}
