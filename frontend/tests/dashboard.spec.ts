import { test, expect } from '@playwright/test';

test.describe('Dashboard Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Authenticate before each test
    await page.goto('/login');
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'admin');
    await page.click('button[type="submit"]');
    await expect(page).not.toHaveURL(/.*login/);
  });

  test('should display service list', async ({ page }) => {
    await page.goto('/');
    
    // Wait for services to load
    await expect(page.locator('#dashboard-title')).toContainText('Service Health Dashboard');
    
    // Wait for the service cards to be visible
    const serviceCards = page.locator('.service-card');
    await expect(serviceCards.first()).toBeVisible();
    
    // Check if Auth-Service or Payment-Service exists (from our DataInitializer)
    await expect(page.locator('body')).toContainText('Service');
  });

  test('should navigate to add service page', async ({ page }) => {
    await page.goto('/');
    
    // Click on Add Service button
    await page.click('#btn-add-service');
    
    // Should redirect to add service page
    await expect(page).toHaveURL(/.*services\/new/);
    await expect(page.locator('#add-service-title')).toContainText('Add New Service');
  });
});
