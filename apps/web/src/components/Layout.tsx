import { Link, useLocation } from 'react-router-dom';
import { ReactNode } from 'react';

interface LayoutProps {
  children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  const location = useLocation();

  const navLinks = [
    { path: '/live', label: 'Live Decision' },
    { path: '/scenarios', label: 'Scenarios' },
    { path: '/dashboard', label: 'Dashboard' },
  ];

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <nav className="border-b border-gray-800 bg-gray-950">
        <div className="container mx-auto px-4 py-3 md:py-4">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-0">
            <Link to="/" className="text-lg sm:text-xl font-bold text-blue-400">
              PodAds Lab
            </Link>
            <div className="flex flex-wrap gap-2 sm:gap-4 md:gap-6">
              {navLinks.map((link) => (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`px-2 sm:px-3 py-1.5 sm:py-2 rounded text-sm sm:text-base transition-colors ${
                    location.pathname === link.path
                      ? 'bg-blue-600 text-white'
                      : 'text-gray-300 hover:text-white hover:bg-gray-800'
                  }`}
                >
                  {link.label}
                </Link>
              ))}
            </div>
          </div>
        </div>
      </nav>
      <main className="container mx-auto px-4 py-4 sm:py-6 md:py-8">{children}</main>
      <footer className="border-t border-gray-800 bg-gray-950 mt-auto py-3 md:py-4">
        <div className="container mx-auto px-4 text-center text-xs sm:text-sm text-gray-400">
          <p>
            Demo by{' '}
            <a 
              href="https://www.linkedin.com/in/sameer-dandekar/" 
              target="_blank" 
              rel="noopener noreferrer"
              className="underline text-gray-300 hover:text-gray-200 transition-colors"
            >
              Sameer Dandekar
            </a>
            .
          </p>
        </div>
      </footer>
    </div>
  );
}


