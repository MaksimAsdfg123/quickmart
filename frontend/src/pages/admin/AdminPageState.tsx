import type { PropsWithChildren, ReactNode } from 'react'

import { Card } from '../../shared/components/ui/Card'
import { EmptyState } from '../../shared/components/ui/EmptyState'
import { Loader } from '../../shared/components/ui/Loader'
import { extractErrorMessage } from '../../shared/lib/errors'

interface AdminPageStateProps {
  isLoading: boolean
  isError: boolean
  error: unknown
  isEmpty: boolean
  loadingLabel: string
  emptyTitle: string
  emptyDescription: string
  emptyAction?: ReactNode
}

export function AdminPageState({
  isLoading,
  isError,
  error,
  isEmpty,
  loadingLabel,
  emptyTitle,
  emptyDescription,
  emptyAction,
  children,
}: PropsWithChildren<AdminPageStateProps>) {
  if (isLoading) {
    return <Loader label={loadingLabel} />
  }

  if (isError) {
    return (
      <Card>
        <div className="error">{extractErrorMessage(error)}</div>
      </Card>
    )
  }

  if (isEmpty) {
    return <EmptyState title={emptyTitle} description={emptyDescription} action={emptyAction} />
  }

  return <>{children}</>
}
