/**
 * Single source of truth for the app's primary navigation.
 * Rendered by the desktop sidebar, the mobile bottom tab bar (primaryMobile items),
 * and the mobile "More" sheet (the rest) so the lists can never drift apart again.
 */
export interface NavItem {
  label: string;
  route: string;
  icon: string;        // lucide icon name (kebab-case)
  exact?: boolean;     // routerLinkActiveOptions.exact
  primaryMobile?: boolean; // shown directly in the mobile bottom tab bar
}

export const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard',   route: '/',                      icon: 'layout-dashboard', exact: true, primaryMobile: true },
  { label: 'Tasks',       route: '/tasks',                 icon: 'check-square',                  primaryMobile: true },
  { label: 'Cleaning',    route: '/cleaning-schedules',    icon: 'calendar-check' },
  { label: 'Expenses',    route: '/expenses',              icon: 'receipt-text' },
  { label: 'History',     route: '/history',               icon: 'clock' },
  { label: 'Statistics',  route: '/statistics',            icon: 'scale' },
  { label: 'Payments',    route: '/payment-status',        icon: 'hand-coins',                   primaryMobile: true },
  { label: 'Shopping',    route: '/shopping-items',        icon: 'shopping-cart',                primaryMobile: true },
  { label: 'My WG',       route: '/household/members',     icon: 'users' },
  { label: 'Invitations', route: '/household/invitations', icon: 'mail' }
];
